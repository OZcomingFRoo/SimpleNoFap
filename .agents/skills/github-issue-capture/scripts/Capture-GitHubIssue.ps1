param(
    [Parameter(Mandatory = $true)]
    [string]$Issue,

    [string]$Repo,

    [string]$OutRoot = ".ai-issues",

    [string]$GhPath = "gh"
)

$ErrorActionPreference = "Stop"

function Resolve-GhPath {
    param([string]$Candidate)

    if (Get-Command $Candidate -ErrorAction SilentlyContinue) {
        return (Get-Command $Candidate).Source
    }

    throw "GitHub CLI not found. Install gh or pass -GhPath."
}

function Resolve-Repo {
    param([string]$ExplicitRepo)

    if ($ExplicitRepo) { return $ExplicitRepo }

    $remote = git remote get-url origin 2>$null
    if (-not $remote) { throw "Repo was not provided and no git origin remote was found." }

    if ($remote -match "github\.com[:/](?<owner>[^/]+)/(?<name>[^/.]+)(\.git)?$") {
        return "$($Matches.owner)/$($Matches.name)"
    }

    throw "Could not infer GitHub repo from origin: $remote"
}

function Get-IssueNumber {
    param([string]$Value)

    if ($Value -match "/issues/(?<n>\d+)") { return $Matches.n }
    if ($Value -match "#(?<n>\d+)$") { return $Matches.n }
    if ($Value -match "^(?<n>\d+)$") { return $Matches.n }

    throw "Could not parse issue number from '$Value'."
}

function Get-AttachmentUrls {
    param([string[]]$Texts)

    $urls = New-Object System.Collections.Generic.List[string]
    foreach ($text in $Texts) {
        if (-not $text) { continue }
        foreach ($match in [regex]::Matches($text, 'https://github\.com/user-attachments/assets/[A-Za-z0-9-]+')) {
            $urls.Add($match.Value)
        }
        foreach ($match in [regex]::Matches($text, '!\[[^\]]*\]\((?<url>https?://[^)]+)\)')) {
            $urls.Add($match.Groups['url'].Value)
        }
        foreach ($match in [regex]::Matches($text, '<img[^>]+src="(?<url>https?://[^"]+)"')) {
            $urls.Add($match.Groups['url'].Value)
        }
    }

    return $urls | Select-Object -Unique
}

function Test-TrustedGitHubAttachmentUrl {
    param([string]$Url)

    try {
        $uri = [Uri]$Url
    } catch {
        return $false
    }

    return $uri.Scheme -eq "https" -and
        $uri.IdnHost -eq "github.com" -and
        $uri.AbsolutePath -match '^/user-attachments/assets/[A-Za-z0-9-]+/?$'
}

function Save-TrustedGitHubAttachment {
    param(
        [string]$Url,
        [string]$Target,
        [string]$Token
    )

    if (-not (Test-TrustedGitHubAttachmentUrl -Url $Url)) {
        throw "Refusing to download a URL outside the trusted GitHub attachment allowlist."
    }

    Add-Type -AssemblyName System.Net.Http
    $handler = New-Object System.Net.Http.HttpClientHandler
    $handler.AllowAutoRedirect = $false
    $client = New-Object System.Net.Http.HttpClient($handler)
    $currentUri = [Uri]$Url

    try {
        for ($redirectCount = 0; $redirectCount -le 5; $redirectCount++) {
            $request = New-Object System.Net.Http.HttpRequestMessage([System.Net.Http.HttpMethod]::Get, $currentUri)
            $request.Headers.Accept.ParseAdd("application/octet-stream")

            # Authentication is permitted only on the original allowlisted GitHub host/path.
            # Redirect requests never inherit it unless their URL independently passes the same allowlist.
            if ($Token -and (Test-TrustedGitHubAttachmentUrl -Url $currentUri.AbsoluteUri)) {
                $request.Headers.Authorization = New-Object System.Net.Http.Headers.AuthenticationHeaderValue("Bearer", $Token)
            }

            $response = $null
            try {
                $response = $client.SendAsync($request).GetAwaiter().GetResult()
                $statusCode = [int]$response.StatusCode

                if ($statusCode -ge 300 -and $statusCode -lt 400 -and $response.Headers.Location) {
                    $nextUri = $response.Headers.Location
                    if (-not $nextUri.IsAbsoluteUri) {
                        $nextUri = New-Object System.Uri -ArgumentList $currentUri, $nextUri
                    }
                    if ($nextUri.Scheme -ne "https") {
                        throw "Refusing non-HTTPS attachment redirect to $nextUri"
                    }
                    $currentUri = $nextUri
                    continue
                }

                if (-not $response.IsSuccessStatusCode) {
                    throw "Attachment download returned HTTP $statusCode."
                }

                $bytes = $response.Content.ReadAsByteArrayAsync().GetAwaiter().GetResult()
                [System.IO.File]::WriteAllBytes($Target, $bytes)
                return $bytes.Length
            } finally {
                if ($response) { $response.Dispose() }
                $request.Dispose()
            }
        }

        throw "Attachment download exceeded the redirect limit."
    } finally {
        $client.Dispose()
        $handler.Dispose()
    }
}

$gh = Resolve-GhPath -Candidate $GhPath
$issueNumber = Get-IssueNumber -Value $Issue
$repoName = Resolve-Repo -ExplicitRepo $Repo
$outDir = Join-Path $OutRoot "issue-$issueNumber"
$attachmentsDir = Join-Path $outDir "attachments"
New-Item -ItemType Directory -Force -Path $outDir, $attachmentsDir | Out-Null

& $gh auth status 1>$null
$issueArgs = @('issue', 'view', $issueNumber, '--repo', $repoName, '--comments', '--json', 'number,title,state,author,createdAt,updatedAt,body,comments,labels,url,assignees,milestone')
$json = & $gh @issueArgs 2>&1
if ($LASTEXITCODE -ne 0) { throw "gh issue view failed: $json" }
$jsonPath = Join-Path $outDir "issue.json"
$json | Set-Content -LiteralPath $jsonPath -Encoding UTF8
$issueData = $json | ConvertFrom-Json

$texts = @($issueData.body)
foreach ($comment in @($issueData.comments | Where-Object { $_ })) { $texts += $comment.body }
$attachmentUrls = @(Get-AttachmentUrls -Texts $texts)

$downloadNotes = New-Object System.Collections.Generic.List[string]
$token = $null

for ($i = 0; $i -lt $attachmentUrls.Count; $i++) {
    $url = $attachmentUrls[$i]

    if (-not (Test-TrustedGitHubAttachmentUrl -Url $url)) {
        $downloadNotes.Add("- $url -> SKIPPED: URL is outside the trusted GitHub attachment allowlist")
        continue
    }

    $fileName = "attachment-$($i + 1).bin"
    if ($url -match '/([^/?#]+)(?:[?#].*)?$') { $fileName = "attachment-$($i + 1)-$($Matches[1]).bin" }
    $target = Join-Path $attachmentsDir $fileName

    try {
        try {
            $length = Save-TrustedGitHubAttachment -Url $url -Target $target
        } catch {
            if (-not $token) {
                $tokenOutput = & $gh auth token 2>&1
                if ($LASTEXITCODE -ne 0) { throw "Could not obtain GitHub authentication for a trusted attachment." }
                $token = ($tokenOutput | Out-String).Trim()
            }
            $length = Save-TrustedGitHubAttachment -Url $url -Target $target -Token $token
        }

        if ($length -le 16) {
            $content = Get-Content -Raw -LiteralPath $target -ErrorAction SilentlyContinue
            throw "Downloaded only $length bytes: $content"
        }
        $downloadNotes.Add("- $url -> attachments/$fileName ($length bytes)")
    } catch {
        $downloadNotes.Add("- $url -> FAILED: $($_.Exception.Message)")
    }
}

$labels = @($issueData.labels | ForEach-Object { $_.name }) -join ", "
if (-not $labels) { $labels = "None" }
$comments = @($issueData.comments | Where-Object { $_ })
$md = @()
$md += "# Issue #$($issueData.number): $($issueData.title)"
$md += ""
$md += "- URL: $($issueData.url)"
$md += "- Repo: $repoName"
$md += "- State: $($issueData.state)"
$md += "- Author: $($issueData.author.login)"
$md += "- Created: $($issueData.createdAt)"
$md += "- Updated: $($issueData.updatedAt)"
$md += "- Labels: $labels"
$md += ""
$md += "## Body"
$md += ""
$md += $issueData.body
$md += ""
$md += "## Attachments"
$md += ""
if ($downloadNotes.Count -gt 0) { $md += $downloadNotes } else { $md += "None found." }
$md += ""
$md += "## Comments"
$md += ""
if ($comments.Count -eq 0) {
    $md += "No comments."
} else {
    foreach ($comment in $comments) {
        $md += "### $($comment.author.login) at $($comment.createdAt)"
        $md += ""
        $md += $comment.body
        $md += ""
    }
}
$md += ""
$md += "## Agent Summary"
$md += ""
$md += "TODO: Inspect attachments and summarize the issue before implementation."

$mdPath = Join-Path $outDir "issue.md"
$md | Set-Content -LiteralPath $mdPath -Encoding UTF8

Write-Output "Captured $repoName#$issueNumber in $outDir"
Write-Output "Issue markdown: $mdPath"
Write-Output "Attachments: $attachmentsDir"
