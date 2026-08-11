libraries{
    maven
    docker {
        java_version = 17
    }
    sonarqube_maven
    archive {
        artifacts = 'mc160_wsdl/target/*.zip, mc160idws_wsdl/target/*.zip, mc160_wsdl/target/resources/wsdl/*.wsdl, mc160idws_wsdl/target/resources/wsdl/*.wsdl'
    }
    verify {
        excluded_list = [
                'CVE-2021-40690', 'CVE-2013-4517', 'CVE-2023-44483', // org.apache.santuario:xmlsec venter på seal
        ]
    }
}