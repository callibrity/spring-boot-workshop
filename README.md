# Spring Boot Workshop

## Prerequisites
- [Git](https://git-scm.com/downloads) installed on your machine
- Java 23 or later ([Adoptium](https://adoptium.net/) recommended)
- [Apache Maven](https://maven.apache.org/) 3.8 or later
- [HTTPie](https://httpie.io/) testing HTTP requests (optional, but recommended)

## Getting Started

### Download the Code

We will use Git to clone the repository.

```bash
git clone https://github.com/callibrity/spring-boot-workshop.git
cd spring-boot-workshop
```

### Build the Application
Now we will use Apache Maven (mvn) to build the project. Maven is a build automation tool used primarily for Java projects, and it will handle downloading dependencies and compiling the code. The first time you run Maven, it will download all the necessary dependencies specified in the `pom.xml` file. Subsequent runs will be faster as Maven caches these dependencies.

```bash
mvn clean install
```

### Run the Application

The Spring Boot Maven plugin provides a `run` gaol that will run the application. You can execute this goal using the following command:

```bash
mvn spring-boot:run
```

When you run the application, you should see output similar to the following:

```text
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.5.0)

2025-06-04T11:24:21.564-04:00  INFO 54352 --- [Spring Workshop] [           main] c.c.s.w.SpringWorkshopApplication        : Starting SpringWorkshopApplication using Java 24.0.1 with PID 54352 (/Users/jcarman/IdeaProjects/spring-workshop/target/classes started by jcarman in /Users/jcarman/IdeaProjects/spring-workshop)
2025-06-04T11:24:21.565-04:00  INFO 54352 --- [Spring Workshop] [           main] c.c.s.w.SpringWorkshopApplication        : No active profile set, falling back to 1 default profile: "default"
2025-06-04T11:24:21.678-04:00  INFO 54352 --- [Spring Workshop] [           main] c.c.s.w.SpringWorkshopApplication        : Started SpringWorkshopApplication in 0.215 seconds (process running for 0.302)
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  0.823 s
[INFO] Finished at: 2025-06-04T11:24:21-04:00
[INFO] ------------------------------------------------------------------------
```

Right now, the application doesn't do much, but we're about to change that by [adding a simple REST API](docs/hello-controller.md)!