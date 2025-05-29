# Используем официальный образ Tomcat
FROM tomcat:9.0-jdk17-openjdk

# Устанавливаем рабочую директорию
WORKDIR /usr/local/tomcat

# Копируем файлы Maven
COPY pom.xml .
COPY src ./src

# Устанавливаем Maven и собираем приложение
RUN apt-get update && \
    apt-get install -y maven && \
    mvn clean package && \
    rm -rf /usr/local/tomcat/webapps/* && \
    cp target/*.war /usr/local/tomcat/webapps/ROOT.war && \
    apt-get remove -y maven && \
    apt-get autoremove -y && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Открываем порт
EXPOSE 8080

# Запускаем Tomcat
CMD ["catalina.sh", "run"] 