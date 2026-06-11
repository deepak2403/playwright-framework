pipeline {
    agent any

    triggers {
        githubPush()
    }

    tools {
        maven 'Maven'
        jdk   'JDK11'
    }

    environment {
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
                withCredentials([file(credentialsId: 'e2e-config-yaml', variable: 'CONFIG_FILE')]) {
                    bat 'copy "%CONFIG_FILE%" src\\test\\resources\\config.yml'
                }
            }
        }

        stage('Build') {
            steps {
                bat 'mvn clean compile -q'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Install Playwright Browsers') {
            steps {
                bat 'mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install chromium"'
            }
        }

        stage('Run API Test') {
            steps {
                withCredentials([file(credentialsId: 'e2e-session', variable: 'SESSION_FILE')]) {
                    bat 'mkdir src\\test\\resources\\session 2>nul & copy "%SESSION_FILE%" src\\test\\resources\\session\\storageState.json'
                    bat 'mvn test -Dsuite=api -Dplaywright.headless=true'
                }
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Run E2E Test') {
            steps {
                withCredentials([file(credentialsId: 'e2e-session', variable: 'SESSION_FILE')]) {
                    bat 'mkdir src\\test\\resources\\session 2>nul & copy "%SESSION_FILE%" src\\test\\resources\\session\\storageState.json'
                    bat 'mvn test -Dsuite=e2e -Dplaywright.headless=true'
                }
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Allure Report') {
            steps {
                bat 'mvn allure:report'
            }
            post {
                always {
                    allure([
                        includeProperties: false,
                        jdk: '',
                        commandline: 'allure',
                        results: [[path: 'allure-results']]
                    ])
                }
            }
        }

    }  // ← closes stages

    post {
        failure {
            echo "Pipeline failed — check test results and Allure report."
        }
        success {
            echo "All tests passed."
        }
        always {
            cleanWs()
        }
    }
}