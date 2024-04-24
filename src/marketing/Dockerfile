FROM openjdk:11-jre-slim

WORKDIR /

COPY ../../bin/marketing /marketing

CMD ["java", "-classpath", "/marketing", "marketing.MarketingDepartment"]
