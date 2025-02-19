
pipeline {
    //agent any
    agent {
        label 'local'
    }
    stages {
        stage('Checkout') {
            steps {
                cleanWs()
                checkout([$class: 'GitSCM', branches: [[name: "*/master"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'fmk-schemas']], submoduleCfg: [], userRemoteConfigs: [[url: 'https://github.com/trifork/fmk-schemas.git',credentialsId: 'github-app-FMK']]])
            }
        }
        stage('build') {
            steps {
                script {
                    String jenkinsUserId = sh(returnStdout: true, script: 'id -u jenkins').trim()
                    String dockerGroupId = sh(returnStdout: true, script: 'getent group docker | cut -d: -f3').trim()
                    String containerUserMapping = "-u $jenkinsUserId:$dockerGroupId "
                    docker.image("registry.fmk.netic.dk/fmk/fmkbuilder_schema:11").inside(containerUserMapping + "--add-host ci.fmk.netic.dk:2a03:dc80:0:f12d::118 -e _JAVA_OPTIONS='-Dfile.encoding=UTF-8 -Djava.net.preferIPv4Stack=false -Djava.net.preferIPv6Stack=true -Djava.net.preferIPv6Addresses=true'") {
                        now = new Date()
                        gitversion = "-${env.GIT_COMMIT}".substring(0,5)
                        version= now.format("YYYYMMddHHmmss")+gitversion
                        sh "echo Building $version"
                        withCredentials([usernamePassword(credentialsId: 'ci_nexus', passwordVariable: 'NexusPass', usernameVariable: 'NexusUser')]) {
                            sh 'cd fmk-schemas && ant -Drepo.user=$NexusUser -Drepo.pass=$NexusPass -v publish'
                        }
                        sh 'cd fmk-schemas && ant validate-all-wsdl'
                    }
                    nexusArtifactUploader(
                            nexusVersion: 'nexus3',
                            protocol: 'https',
                            nexusUrl: 'ci.fmk.netic.dk/nexus',
                            groupId: 'com.trifork',
                            version: "${version}",
                            repository: 'trifork-internal',
                            credentialsId: 'ci_nexus',
                            artifacts: [
                                    [artifactId: 'fmk-schema-admin-classes',
                                     classifier: '',
                                     file: 'fmk-schemas/target/fmk-schema-admin-classes.jar',
                                     type: 'jar'],
                                    [artifactId: 'fmk-schema-admin',
                                     classifier: '',
                                     file: 'fmk-schemas/target/fmk-schema-admin.zip',
                                     type: 'zip'],
                                    [artifactId: 'fmk-schema-classes',
                                     classifier: '',
                                     file: 'fmk-schemas/target/fmk-schema-classes.jar',
                                     type: 'jar'],
                                    [artifactId: 'fmk-schema-dist',
                                     classifier: '',
                                     file: 'fmk-schemas/target/fmk-schema-dist.zip',
                                     type: 'zip'],
                                    [artifactId: 'fmk-schema-idws-xua',
                                     classifier: '',
                                     file: 'fmk-schemas/target/fmk-schema-idws-xua.zip',
                                     type: 'zip'],
                                    [artifactId: 'fmk-schema-idws',
                                     classifier: '',
                                     file: 'fmk-schemas/target/fmk-schema-idws.zip',
                                     type: 'zip'],
                                    [artifactId: 'fmk-schema',
                                     classifier: '',
                                     file: 'fmk-schemas/target/fmk-schema.zip',
                                     type: 'zip'],
                                    [artifactId: 'fmk-schema-testtool-classes',
                                     classifier: '',
                                     file: 'fmk-schemas/target/fmk-schema-testtool-classes.jar',
                                     type: 'jar'],
                                    [artifactId: 'fmk-schema-testtool',
                                     classifier: '',
                                     file: 'fmk-schemas/target/fmk-schema-testtool.zip',
                                     type: 'zip'],
                                    [artifactId: 'fmk-schema-source.zip',
                                     classifier: '',
                                     file: 'fmk-schemas/target/fmk-schema-source.zip',
                                     type: 'zip'],
                                    [artifactId: 'NamespaceOracle-generated.properties',
                                     classifier: '',
                                     file: 'fmk-schemas/target/NamespaceOracle-generated.properties',
                                     type: 'properties'],
                                    [artifactId: 'fmk-schema-classes-deps',
                                     classifier: '',
                                     file: 'fmk-schemas/target/fmk-schema-classes-deps.jar',
                                     type: 'jar']
                            ]
                    )
                    archiveArtifacts 'fmk-schemas/target/wsdl/*.zip, fmk-schemas/target/standard/target/wsdl/*.zip, fmk-schemas/target/idws/target/wsdl/*.zip, fmk-schemas/target/standard/target/wsdl/*.wsdl, fmk-schemas/target/idws/target/wsdl/*.wsdl'

                }
            }
        }
    }
    post {
        failure {
            emailext body: '$DEFAULT_CONTENT',
                    recipientProviders: [culprits(), requestor()],
                    subject: '$DEFAULT_SUBJECT'
        }
        unstable {
            emailext body: '$DEFAULT_CONTENT',
                    recipientProviders: [culprits(), requestor()],
                    subject: '$DEFAULT_SUBJECT'
        }
        fixed {
            emailext body: '$DEFAULT_CONTENT',
                    recipientProviders: [culprits(), requestor()],
                    subject: '$DEFAULT_SUBJECT'
        }
    }
}



