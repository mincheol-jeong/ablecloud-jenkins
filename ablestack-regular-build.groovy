import java.text.SimpleDateFormat
def NEW_DATE = "null"
pipeline {
    agent any

    environment {
        JWF = '/mnt/jenkins-work'
        BRF = '/mnt/jenkins-work/build'
        SF = '/mnt/jenkins-work/service'
        SBINF = '/mnt/jenkins-work/sbin'
    }

    stages {
       stage('Version Change') {
           steps {
               
               script {
                   sh('rm -rf ${BRF}/*')
                   def versionInfo = readFile(file: '/mnt/jenkins-work/versionInfo.txt')
                   println(versionInfo)
                   def (codeName, version, date) = versionInfo.split('-')
                   println(codeName+version+date)
                   def (a,b,c) = version.split('\\.')
                   int intC = c as int
                   intC = intC +1
                   println(intC)
                   def dateFormat = new SimpleDateFormat('yyMMdd')
                   def today = new Date()
                   def newDate = dateFormat.format(today)
                   def newVersionInfo = codeName + '-' + a + '.' + b + '.' + intC.toString() + '-' + newDate
                   println('newVersionInfo : '+newVersionInfo)
                   writeFile file: '/mnt/jenkins-work/versionInfo.txt', text: newVersionInfo

                   sh('cat ${JWF}/versionInfo.txt')
                   sh('rm -rf ${JWF}/'+newDate)
                   sh('mkdir ${JWF}/'+newDate)
                   NEW_DATE=newDate
               }
           }
       }

       stage('Cockpit Build') {
           steps{

               build 'cockpit'
           }
       }
        
        stage('Cockpit-plugin Build') {
           steps{

               build 'cockpit-plugin'
           }
       }

        stage('Glue Build') {
            steps{

                build 'glue-build'
            }
       }

       stage('Glue RPM Copy to mirroring') {
           steps{

               sh(""" rm -rf /root/mirror/data/centos/glue/* """)
               sh(""" cp -rf ${BRF}/*Glue*.rpm /root/mirror/data/centos/glue """)
               sh(""" createrepo /root/mirror/data/centos/glue/. """)
           }
       }

       stage('Glue Image Build And DockerHub Push') {
           steps{

               build 'glue-image'
           }
       }

        stage('Mold Build') {
            steps{

                build 'mold'
            }
        }

        stage('Netdive Build') {
            steps{

                build 'netdive-ui'
            }
        }

        stage('Wall Build') {
            steps{

                build 'wall-build'
            }
        }

       stage('Build result file Move to version folder') {
           steps{

                  sh("""cp -r ${BRF}/* ${JWF}/${NEW_DATE}/""")
           }
       }

        stage('Ablestack Template Create') {
            steps{

                build 'make-qcow2-template'
            }
        }
        
        stage('Ablestack ISO Create') {
            steps{

                build 'ablestack-kickstart'
            }
        }
        
        stage('Ablestack ISO Copy') {
            steps{
                sh("""cp -r ${JWF}/ISO/* ${JWF}/${NEW_DATE}/""")
                sh("""rm -rf ${JWF}/ISO/*""")
            }
        }
    }
}
