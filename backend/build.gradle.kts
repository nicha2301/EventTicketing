plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.5.3"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.springdoc.openapi-gradle-plugin") version "1.9.0"
	kotlin("plugin.jpa") version "1.9.25"
	jacoco
}

group = "com.eventticketing"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("org.springdoc:springdoc-openapi-kotlin:1.8.0")
	implementation("io.jsonwebtoken:jjwt-api:0.12.6")
	implementation("org.springframework.boot:spring-boot-starter-mail:3.5.3")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.liquibase:liquibase-core")
	
	// Apache POI for Excel exports
	implementation("org.apache.poi:poi:5.2.5")
	implementation("org.apache.poi:poi-ooxml:5.2.5")
	
	// iText for PDF exports
	implementation("com.itextpdf:itextpdf:5.5.13.3")
	implementation("com.itextpdf:html2pdf:4.0.5")
	
	// JSON processing
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
	implementation("org.json:json:20240303")
	
	// Redis and Caching
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.springframework.boot:spring-boot-starter-cache")
	implementation("io.lettuce:lettuce-core")
	
	compileOnly("org.projectlombok:lombok")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	runtimeOnly("io.micrometer:micrometer-registry-prometheus")
	runtimeOnly("org.postgresql:postgresql")
	annotationProcessor("org.projectlombok:lombok")
	
	// Testing dependencies
	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(module = "mockito-core")
	}
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
	testImplementation("io.mockk:mockk:1.13.10")
	testImplementation("com.ninja-squad:springmockk:4.0.2")
	testImplementation("org.testcontainers:testcontainers:1.19.7")
	testImplementation("org.testcontainers:junit-jupiter:1.19.7")
	testImplementation("org.testcontainers:postgresql:1.19.7")
	testImplementation("com.h2database:h2:2.2.224")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
	useJUnitPlatform()
	finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
	dependsOn(tasks.test)
	reports {
		xml.required = true
		csv.required = false
		html.outputLocation = layout.buildDirectory.dir("jacocoHtml")
	}
}

tasks.jacocoTestCoverageVerification {
	violationRules {
		rule {
			limit {
				minimum = "0.8".toBigDecimal()
			}
		}
	}
}
