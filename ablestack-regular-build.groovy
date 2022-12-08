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
                   def dateFormat = new SimpleDateFormat('MMdd')
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


       stage('Glue RPM Copy to mirroring') {
           steps{

               sh(""" rm -rf /root/mirror/data/centos/glue/* """)
               sh(""" cp -rf ${BRF}/*Glue*.rpm /root/mirror/data/centos/glue """)
               sh(""" createrepo /root/mirror/data/centos/glue/. """)
           }
       }

     
        }
    }
