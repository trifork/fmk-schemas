libraries{
    maven
    docker {
        java_version = 11
    }
    sonarqube_maven
    archive {
        artifacts = 'mc160_wsdl/target/*.zip, mc160idws_wsdl/target/*.zip, mc160_wsdl/target/resources/wsdl/*.wsdl, mc160idws_wsdl/target/resources/wsdl/*.wsdl'
    }
}