BACKEND
PARA SPRING UTILICE LA JAVA 17 
Y LA VERSION 3.4.4 DE SPRINGBOOT

BASE DE DATOS UTILIZADA MySQL : POR ERRORES DE COMPATIBILIDAD
EN CASO DE QUE FALLE EL Flyway CAMBIAR A FALSE PARA QUE CORRA EL PROGRAMA
# Flyway para migraciones
#spring.flyway.enabled=true
# spring.flyway.locations=classpath:db/migration
APPLICATION.PROPERTIES
spring.application.name=backend-challenge
spring.datasource.url=jdbc:mysql://localhost:3306/test_challenge
#spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
spring.datasource.username=root
spring.datasource.password=sasa1234
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
#spring.jpa.database-platform=org.hibernate.dialect.H2Dialect

# Hibernate: crea las tablas y luego las elimina al finalizar los tests
# spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.hibernate.ddl-auto=update
SE LE ADJUNTA UNA COLLECCION EN POSTMAN PARA QUE PUEDA REVISAR CADA ENDPOINT.

LIMPIAR TODAS LAS DEPENDENCIAS Y EJECUTAR LAS PRUEBAS 
mvn clean install  
