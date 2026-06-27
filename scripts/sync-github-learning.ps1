param(
    [string]$PlanPath = ".github/learning-plan.json",
    [string]$CommitMessage = "Add Spring learning notes",
    [string]$PrTitle = "Add Spring learning notes",
    [string]$BranchName = "",
    [string]$BaseBranch = "",
    [switch]$SkipPullRequest,
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"

function Invoke-Gh {
    param(
        [Parameter(Mandatory = $true)]
        [string[]]$Arguments
    )

    if ($DryRun) {
        Write-Host "[dry-run] gh $($Arguments -join ' ')"
        return ""
    }

    $output = & gh @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "gh command failed: gh $($Arguments -join ' ')"
    }
    return ($output | Out-String).Trim()
}

function Invoke-Git {
    param(
        [Parameter(Mandatory = $true)]
        [string[]]$Arguments
    )

    if ($DryRun) {
        Write-Host "[dry-run] git $($Arguments -join ' ')"
        return ""
    }

    $output = & git @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "git command failed: git $($Arguments -join ' ')"
    }
    return ($output | Out-String).Trim()
}

function Get-RepoName {
    if ($DryRun) {
        return "OWNER/REPO"
    }

    $repo = Invoke-Gh @("repo", "view", "--json", "nameWithOwner", "--jq", ".nameWithOwner")
    if ([string]::IsNullOrWhiteSpace($repo)) {
        throw "GitHub repository not found. Add an origin remote first, then run: gh auth login"
    }
    return $repo.Trim()
}

function Get-DefaultBranch {
    if (-not [string]::IsNullOrWhiteSpace($BaseBranch)) {
        return $BaseBranch
    }

    if ($DryRun) {
        return "main"
    }

    $branch = Invoke-Gh @("repo", "view", "--json", "defaultBranchRef", "--jq", ".defaultBranchRef.name")
    if ([string]::IsNullOrWhiteSpace($branch)) {
        return "main"
    }
    return $branch.Trim()
}

function Get-CurrentBranch {
    if ($DryRun) {
        return "main"
    }

    $branch = Invoke-Git @("branch", "--show-current")
    return $branch.Trim()
}

function New-LearningBranchName {
    if (-not [string]::IsNullOrWhiteSpace($BranchName)) {
        return $BranchName
    }

    return "learning/" + (Get-Date -Format "yyyyMMdd-HHmmss")
}

function Get-IssueNumberFromUrl {
    param([string]$Url)

    if ($Url -match "/issues/(\d+)$") {
        return $Matches[1]
    }
    return ""
}

function Ensure-Labels {
    param($Plan)

    foreach ($label in $Plan.labels) {
        $existingLabel = Invoke-Gh @(
            "label", "list",
            "--search", $label.name,
            "--json", "name",
            "--jq", ".[] | select(.name == `"$($label.name)`") | .name"
        )

        if ([string]::IsNullOrWhiteSpace($existingLabel)) {
            Write-Host "Create label: $($label.name)"
            Invoke-Gh @("label", "create", $label.name, "--color", $label.color, "--description", $label.description) | Out-Null
        } else {
            Write-Host "Label exists: $($label.name)"
        }
    }
}

function Ensure-Milestones {
    param($Plan, [string]$Repo)

    foreach ($milestone in $Plan.milestones) {
        $existingMilestone = Invoke-Gh @(
            "api", "repos/$Repo/milestones",
            "--jq", ".[] | select(.title == `"$($milestone.title)`") | .number"
        )

        if ([string]::IsNullOrWhiteSpace($existingMilestone)) {
            Write-Host "Create milestone: $($milestone.title)"
            Invoke-Gh @(
                "api", "repos/$Repo/milestones",
                "-f", "title=$($milestone.title)",
                "-f", "description=$($milestone.description)"
            ) | Out-Null
        } else {
            Write-Host "Milestone exists: $($milestone.title)"
        }
    }
}

function Ensure-Issues {
    param($Plan, [string]$Repo)

    $issueNumbers = @()

    foreach ($issue in $Plan.issues) {
        if (-not (Test-Path -LiteralPath $issue.body)) {
            throw "Issue body file not found: $($issue.body)"
        }

        $existingIssue = Invoke-Gh @(
            "issue", "list",
            "--repo", $Repo,
            "--state", "all",
            "--search", "$($issue.id) in:body",
            "--json", "number",
            "--jq", ".[0].number"
        )

        if (-not [string]::IsNullOrWhiteSpace($existingIssue)) {
            Write-Host "Issue exists: #$existingIssue $($issue.title)"
            $issueNumbers += $existingIssue.Trim()
            continue
        }

        $args = @("issue", "create", "--repo", $Repo, "--title", $issue.title, "--body-file", $issue.body)

        if (-not [string]::IsNullOrWhiteSpace($issue.milestone)) {
            $args += @("--milestone", $issue.milestone)
        }

        foreach ($label in $issue.labels) {
            $args += @("--label", $label)
        }

        Write-Host "Create issue: $($issue.title)"
        $issueUrl = Invoke-Gh $args
        $issueNumber = Get-IssueNumberFromUrl $issueUrl
        if (-not [string]::IsNullOrWhiteSpace($issueNumber)) {
            $issueNumbers += $issueNumber
        }
    }

    return $issueNumbers
}

function Publish-PullRequest {
    param(
        [string]$Repo,
        [string]$Base,
        [string]$Head,
        [string[]]$IssueNumbers
    )

    $status = Invoke-Git @("status", "--porcelain")
    if ($DryRun) {
        $status = "DRYRUN"
    }
    if ([string]::IsNullOrWhiteSpace($status)) {
        Write-Host "No file changes to publish. Skipping commit and PR."
        return ""
    }

    $currentBranch = Get-CurrentBranch
    if ($currentBranch -ne $Head) {
        $branchExists = Invoke-Git @("branch", "--list", $Head)
        if ([string]::IsNullOrWhiteSpace($branchExists)) {
            Invoke-Git @("switch", "-c", $Head) | Out-Null
        } else {
            Invoke-Git @("switch", $Head) | Out-Null
        }
    }

    Invoke-Git @("add", "-A") | Out-Null
    Invoke-Git @("commit", "-m", $CommitMessage) | Out-Null
    Invoke-Git @("push", "-u", "origin", $Head) | Out-Null

    if ($SkipPullRequest) {
        Write-Host "Pull request creation skipped."
        return ""
    }

    $refs = ""
    if ($IssueNumbers.Count -gt 0) {
        $refs = ($IssueNumbers | ForEach-Object { "Refs #$_" }) -join "`n"
    }

    $bodyFile = New-TemporaryFile
    @"
## Summary

- Sync learning labels, milestones, and issues.
- Add or update Spring study notes and generated HTML outputs.

## Issues

$refs
"@ | Set-Content -LiteralPath $bodyFile -Encoding UTF8

    $prUrl = Invoke-Gh @(
        "pr", "create",
        "--repo", $Repo,
        "--base", $Base,
        "--head", $Head,
        "--title", $PrTitle,
        "--body-file", $bodyFile
    )

    Remove-Item -LiteralPath $bodyFile -Force
    return $prUrl
}

if (-not (Test-Path -LiteralPath $PlanPath)) {
    throw "Plan file not found: $PlanPath"
}

$plan = Get-Content -Raw -LiteralPath $PlanPath | ConvertFrom-Json
$repo = Get-RepoName
$base = Get-DefaultBranch
$head = New-LearningBranchName

Write-Host "Repository: $repo"
Write-Host "Base branch: $base"
Write-Host "Working branch: $head"

Ensure-Labels $plan
Ensure-Milestones $plan $repo
$issueNumbers = Ensure-Issues $plan $repo
$prUrl = Publish-PullRequest $repo $base $head $issueNumbers

if (-not [string]::IsNullOrWhiteSpace($prUrl)) {
    Write-Host ""
    Write-Host "Pull request created:"
    Write-Host $prUrl
}
