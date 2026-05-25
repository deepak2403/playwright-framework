pipeline {
    agent any

// pipeline version 1.0
    triggers {
        // Triggers on every push via GitHub webhook
        githubPush()
    }

    tools {
        maven 'Maven'   // Must match the name configured in Jenkins → Global Tool Config
        jdk   'JDK11'   // Must match the name configured in Jenkins → Global Tool Config
    }

    environment {
        // Pull all test credentials from Jenkins credentials store
        USER1_EMAIL              = credentials('USER1_EMAIL')
        USER1_PASSWORD           = credentials('USER1_PASSWORD')
        USER1_SEARCH_TERM        = credentials('USER1_SEARCH_TERM')
        USER1_BRAND              = credentials('USER1_BRAND')
        USER1_SECONDARY_KEYWORD  = credentials('USER1_SECONDARY_KEYWORD')
        USER1_RAM_SIZE           = credentials('USER1_RAM_SIZE')
        USER1_RAM_TYPE           = credentials('USER1_RAM_TYPE')

        USER2_EMAIL              = credentials('USER2_EMAIL')
        USER2_PASSWORD           = credentials('USER2_PASSWORD')
        USER2_SEARCH_TERM        = credentials('USER2_SEARCH_TERM')
        USER2_BRAND              = credentials('USER2_BRAND')
        USER2_SECONDARY_KEYWORD  = credentials('USER2_SECONDARY_KEYWORD')
        USER2_RAM_SIZE           = credentials('USER2_RAM_SIZE')
        USER2_RAM_TYPE           = credentials('USER2_RAM_TYPE')
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean compile -q'
            }
        }

        stage('Install Playwright Browsers') {
            steps {
                sh 'mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install chromium"'
            }
        }

        stage('Run API Tests') {
            steps {
                sh 'mvn test -Dsuite=api'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Run E2E Tests') {
            steps {
                // Headless mode for CI — no display available on Jenkins agent
                sh 'mvn test -Dsuite=e2e -Dplaywright.headless=true'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Allure Report') {
            steps {
                sh 'mvn allure:report'
            }
            post {
                always {
                    allure([
                        includeProperties: false,
                        jdk: '',
                        results: [[path: 'allure-results']]
                    ])
                }
            }
        }
    }

    post {
        failure {
            echo "Pipeline failed — check test results and Allure report."
        }
        success {
            echo "All tests passed."
        }
        always {
            // Clean workspace after run to avoid session file bleed between builds
            cleanWs()
        }
    }
}