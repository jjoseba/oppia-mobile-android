  workflow "Build and Test" {
    on = "push"
    resolves = "Publish Code Coverage"
  }

    action "Build pass" {
    uses = "vgaidarji/android-github-actions/build@v1.0.0"
    secrets = ["FABRIC_API_KEY", "FABRIC_API_SECRET"]
    args = "./gradlew assembleDebug -PpreDexEnable=false"
  }

    action "Check" {
    needs = ["Build"]
    uses = "vgaidarji/android-github-actions/build@v1.0.0"
    args = "./gradlew testDebug jacocoTestReport checkstyle pmd jdepend lintDebug buildDashboard -PpreDexEnable=false"
  }

    action "Run UI Tests" {
    needs = ["Build"]
    uses = "vgaidarji/android-github-actions/emulator@v1.0.0"
  }
