FROM openjdk:21

WORKDIR /usrapp/bin

ENV PORT=6000
ENV MAX_THREADS=10

# Copiar las clases compiladas
COPY /target/classes /usrapp/bin/classes

# Copiar los recursos estáticos desde el directorio fuente
COPY /src/main/java/resources /usrapp/bin/src/main/java/resources

CMD ["java","-cp","./classes","co.edu.escuelaing.microsptingboot.MicroSptingBoot"]