def isMaster = env.BRANCH_NAME == 'master'
def skipAllStages() {
    return "${env.SKIP_BUILD}" == "true"
}

pipeline {
    agent {
        docker {
            image 'onef-docker-registry.jfrog.io/ml-build-env:0.1.0'
            label 'agent-with-docker'
            args "-u root " +
                    "--entrypoint=/entrypoint.sh " +
                    "-v ivy-home-${"${env.JOB_NAME}".replaceAll("\\/", "--")}:/root/.ivy2 " +
                    "-v global-sbt-boot:/root/.sbt/boot " +
                    "-v global-sbt-coursier-cache:/root/.coursier/cache " +
                    "-v global-pip-cache:/root/.cache/pip " +
                    "-v global-pipenv-cache:/root/.cache/pipenv " +
                    "-v /var/run/docker.sock:/var/run/docker.sock"
        }
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
        ansiColor('xterm2')
    }

    environment {
        KRB5_PASS = credentials('ml-engine-krb5pass')
        CONSUL_TOKEN = credentials('geo-analytics-test-consul-token')
        AGENT_HOSTNAME = sh(returnStdout: true, script: "hostname -I | cut -d' ' -f1").trim()
        ARTIFACTORY_USERNAME = "uploader"
        SBT_OPTS = "-Xmx3G"
    }

    parameters {
        booleanParam(name: 'RELEASE', defaultValue: false, description: 'Release current version')
        booleanParam(name: 'PACKAGE', defaultValue: true, description: 'Package jars')
        booleanParam(name: 'RUN_TEST', defaultValue: true, description: 'Run unit and integration tests')
        booleanParam(name: 'PUBLISH', defaultValue: !isMaster, description: 'Publish jars')
        booleanParam(name: 'DEPLOY_RELEASE', defaultValue: false, description: 'Deploy images to prod environment')
    }

    stages {

        stage('Prepare env') {
            steps {
                script {
                    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: '560a8652-d4c5-405f-ac8b-4569ff0f6381', usernameVariable: 'GIT_USERNAME', passwordVariable: 'GIT_PASSWORD']]) {
                        env.TECH_COMMIT = sh(script: "git log -n 1 --pretty=format:'%an' | grep ${env.GIT_USERNAME}", returnStatus: true) == 0

                        // booleans doesn't work...
                        if (currentBuild.rawBuild.getCause(jenkins.branch.BranchEventCause) && "${env.TECH_COMMIT}" == "true") {
                            println "Info: This is technical commit. Skipping..."
                            env.SKIP_BUILD = "true"
                        } else {
                            env.SKIP_BUILD = "false"
                        }
                    }
                }
                sh "printenv"

            }
        }

        stage('Skip on tech') {
            when {
                expression { return skipAllStages }
            }
            steps {
                sh "echo 'skipAllStages == true'"
            }
        }

        stage('Unskip on tech') {
            when {
                expression { return !skipAllStages }
            }
            steps {
                sh "echo 'skipAllStages == false'"
            }
        }

        stage('Checkout') {
            /*when {
                expression { return env.TECH_COMMIT }
            }*/
            steps {
                checkout scm

                //sh 'git config -l'
                sh 'sbt clean'
            }
        }

        stage('Release') {
            when {
                expression { return params.RELEASE && env.BRANCH_NAME == 'master' }
            }
            steps {
                sh 'git config user.name "jenkins-1f"'
                sh 'git config user.email "jenkins@onefactor.com"'
                sh 'sbt "release with-defaults default-tag-exists-answer o"'
            }
        }

        stage('Package') {
            when {
                expression { return (params.PACKAGE ) }
            }
            steps {
                sh 'sbt package'
            }
        }

        stage('Test') {
            when {
                expression { return (params.RUN_TEST && !params.RELEASE ) }
            }
            steps {
                sh 'sbt test'

            }
        }

        stage('Publish') {
            when {
                expression { return params.PUBLISH && !env.TECH_COMMIT}
            }
            steps {
                sh 'sbt publish'
            }
        }


        stage('Post release') {
            when {
                expression { return params.RELEASE && env.BRANCH_NAME == 'master' }
            }
            steps {
                sh 'sbt postRelease'
                script {
                    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: '560a8652-d4c5-405f-ac8b-4569ff0f6381', usernameVariable: 'GIT_USERNAME', passwordVariable: 'GIT_PASSWORD']]) {
                        sh "git push https://${env.GIT_USERNAME}:${env.GIT_PASSWORD}@github.com/kycml/sbt-release-test.git HEAD:master --follow-tags"
                    }
                }
            }
        }

        stage('Deploy release') {
            when {
                expression { return params.DEPLOY_RELEASE && env.BRANCH_NAME == 'master' }
            }
            environment {
                RANCHER_SECRET_KEY = credentials('ml-engine_secret_key_prod_rancher')
                RANCHER_ENV = 'prod'
                RANCHER_STACK_NAME = 'sm-ml-engine'
            }
            steps {
                sh 'sbt deploy'
            }
        }
    }
}
