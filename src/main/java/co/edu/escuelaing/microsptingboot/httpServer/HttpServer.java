package co.edu.escuelaing.microsptingboot.httpServer;


import co.edu.escuelaing.microsptingboot.annotations.GetMapping;
import co.edu.escuelaing.microsptingboot.annotations.RequestParam;
import co.edu.escuelaing.microsptingboot.annotations.RestController;
import java.net.*;
import java.io.*;
import java.lang.reflect.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HttpServer {

    private static int PORT = Integer.parseInt(System.getenv().getOrDefault("PORT", "35000"));

    private static String basePath = "src/main/java/resources/";

    public static Map<String, Method> services = new HashMap<>();

    // Pool de hilos para manejar peticiones concurrentes
    private static ExecutorService threadPool;

    // Número máximo de hilos concurrentes
    private static final int MAX_THREADS = Integer.parseInt(System.getenv().getOrDefault("MAX_THREADS", "10"));

    public static void loadServices(String[] args) {
        // Si se proporciona un parámetro específico, usar el método original
        if (args != null && args.length > 0) {
            loadSpecificController(args[0]);
        } else {
            // Explorar automáticamente el classpath
            loadAllRestControllers();
        }
    }

    /**
     * Método original para cargar un controlador específico
     */
    private static void loadSpecificController(String className) {
        try {
            Class c = Class.forName(className);
            loadControllerMethods(c);
        } catch (ClassNotFoundException ex) {
            System.getLogger(HttpServer.class.getName()).log(System.Logger.Level.ERROR, "Controller class not found: " + className, ex);
        }
    }

    /**
     * Explorar automáticamente todas las clases con @RestController
     */
    private static void loadAllRestControllers() {
        Set<Class<?>> restControllers = findRestControllers();
        for (Class<?> controller : restControllers) {
            loadControllerMethods(controller);
        }
        System.out.println("Loaded " + restControllers.size() + " REST controllers automatically");
    }

    /**
     * Cargar métodos de un controlador específico
     */
    private static void loadControllerMethods(Class<?> c) {
        if (c.isAnnotationPresent(RestController.class)) {
            Method[] methods = c.getDeclaredMethods();
            for (Method m : methods) {
                if (m.isAnnotationPresent(GetMapping.class)) {
                    String mapping = m.getAnnotation(GetMapping.class).value();
                    services.put(mapping, m);
                    System.out.println("Registered endpoint: " + mapping + " -> " + c.getSimpleName() + "." + m.getName());
                }
            }
        }
    }

    /**
     * Encontrar todas las clases con la anotación @RestController
     */
    private static Set<Class<?>> findRestControllers() {
        Set<Class<?>> controllers = new HashSet<>();

        try {
            // Obtener el ClassLoader actual
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String packageName = "co.edu.escuelaing.microsptingboot";

            // Convertir el nombre del paquete a un path
            String path = packageName.replace('.', '/');
            Enumeration<URL> resources = classLoader.getResources(path);

            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                if (resource.getProtocol().equals("file")) {
                    // Explorar directorio del sistema de archivos
                    File directory = new File(resource.toURI());
                    controllers.addAll(findClassesInDirectory(directory, packageName));
                } else if (resource.getProtocol().equals("jar")) {
                    // Explorar archivo JAR
                    controllers.addAll(findClassesInJar(resource, packageName));
                }
            }
        } catch (Exception e) {
            System.err.println("Error explorando el classpath: " + e.getMessage());
            e.printStackTrace();
        }

        return controllers;
    }

    /**
     * Encontrar clases en un directorio del sistema de archivos
     */
    private static Set<Class<?>> findClassesInDirectory(File directory, String packageName) {
        Set<Class<?>> controllers = new HashSet<>();

        if (!directory.exists()) {
            return controllers;
        }

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // Recursivamente explorar subdirectorios
                    controllers.addAll(findClassesInDirectory(file, packageName + "." + file.getName()));
                } else if (file.getName().endsWith(".class")) {
                    // Procesar archivo .class
                    String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                    try {
                        Class<?> clazz = Class.forName(className);
                        if (clazz.isAnnotationPresent(RestController.class)) {
                            controllers.add(clazz);
                        }
                    } catch (ClassNotFoundException e) {
                        // Ignorar clases que no se pueden cargar
                    }
                }
            }
        }

        return controllers;
    }

    /**
     * Encontrar clases en un archivo JAR
     */
    private static Set<Class<?>> findClassesInJar(URL jarUrl, String packageName) {
        Set<Class<?>> controllers = new HashSet<>();

        try {
            String jarPath = jarUrl.getPath().substring(5, jarUrl.getPath().indexOf("!"));
            JarFile jarFile = new JarFile(jarPath);
            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();

                if (entryName.endsWith(".class") && entryName.startsWith(packageName.replace('.', '/'))) {
                    String className = entryName.replace('/', '.').substring(0, entryName.length() - 6);
                    try {
                        Class<?> clazz = Class.forName(className);
                        if (clazz.isAnnotationPresent(RestController.class)) {
                            controllers.add(clazz);
                        }
                    } catch (ClassNotFoundException e) {
                        // Ignorar clases que no se pueden cargar
                    }
                }
            }

            jarFile.close();
        } catch (Exception e) {
            System.err.println("Error procesando JAR: " + e.getMessage());
        }

        return controllers;
    }

    /**
     * The main method to create the http server with concurrent request handling
     *
     * @param args
     * @throws IOException
     * @throws URISyntaxException
     */
    public static void startServer(String[] args) throws IOException, URISyntaxException {

        loadServices(args);

        // Inicializar el pool de hilos
        threadPool = Executors.newFixedThreadPool(MAX_THREADS);
        System.out.println("Server configured with " + MAX_THREADS + " concurrent threads");

        // Agregar shutdown hook para cerrar gracefully el pool de hilos
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down server...");
            shutdownServer();
        }));

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started on port: " + PORT);
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + PORT + ".");
            System.exit(1);
        }

        boolean running = true;

        // Recibe múltiples solicitudes concurrentemente
        while (running) {
            try {
                // Acepta la solicitud del cliente
                System.out.println("Listo Para Recibir...");
                Socket clientSocket = serverSocket.accept();

                // Crear y enviar tarea al pool de hilos
                threadPool.submit(new ClientHandler(clientSocket));

            } catch (IOException e) {
                System.err.println("Accept failed: " + e.getMessage());
                if (serverSocket != null && !serverSocket.isClosed()) {
                    continue; // Continuar aceptando conexiones
                } else {
                    break; // Salir si el socket está cerrado
                }
            }
        }

        // Cerrar el servidor
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
        shutdownServer();
    }

    /**
     * Cierra gracefully el pool de hilos
     */
    private static void shutdownServer() {
        if (threadPool != null && !threadPool.isShutdown()) {
            threadPool.shutdown();
            try {
                // Esperar hasta 30 segundos para que terminen las tareas actuales
                if (!threadPool.awaitTermination(30, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow();
                    // Esperar otros 30 segundos para terminación forzada
                    if (!threadPool.awaitTermination(30, TimeUnit.SECONDS)) {
                        System.err.println("Thread pool did not terminate");
                    }
                }
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Clase interna para manejar cada cliente en un hilo separado
     */
    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            String threadName = Thread.currentThread().getName();
            System.out.println("Thread " + threadName + " handling client: " + clientSocket.getRemoteSocketAddress());

            try {
                handleClient(clientSocket);
            } catch (Exception e) {
                System.err.println("Error handling client in thread " + threadName + ": " + e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    if (!clientSocket.isClosed()) {
                        clientSocket.close();
                    }
                } catch (IOException e) {
                    System.err.println("Error closing client socket in thread " + threadName + ": " + e.getMessage());
                }
                System.out.println("Thread " + threadName + " finished handling client");
            }
        }

        /**
         * Maneja la comunicación con un cliente específico
         */
        private void handleClient(Socket clientSocket) throws IOException {
            // create the IO streams
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));

            String inputLine;
            boolean isFirstLine = true;
            URI requestUri = null;

            try {
                while ((inputLine = in.readLine()) != null) {
                    if (isFirstLine) {
                        // get the URI
                        try {
                            requestUri = new URI(inputLine.split(" ")[1]);
                            System.out.println("Thread " + Thread.currentThread().getName() +
                                             " - Path: " + requestUri.getPath());
                            isFirstLine = false;
                        } catch (Exception e) {
                            System.err.println("Invalid request line: " + inputLine);
                            return;
                        }
                    }
                    System.out.println("Thread " + Thread.currentThread().getName() +
                                     " - Received: " + inputLine);
                    if (!in.ready()) {
                        break;
                    }
                }

                if (requestUri != null) {
                    handlerequestType(requestUri, out, clientSocket.getOutputStream());
                }

            } catch (Exception e) {
                System.err.println("Error processing request: " + e.getMessage());
            } finally {
                try {
                    out.close();
                    in.close();
                } catch (IOException e) {
                    System.err.println("Error closing streams: " + e.getMessage());
                }
            }
        }
    }

    /**
     * This method handle the request and its response by reading its file type
     *
     * @param requestUri
     * @param out
     * @param outputStream
     */
    private static void handlerequestType(URI requestUri, PrintWriter out, OutputStream outputStream) {
        if (requestUri.getPath().endsWith(".html") || requestUri.getPath().equalsIgnoreCase("/")) {
            getHTML(requestUri, out);
        } else if (requestUri.getPath().endsWith(".css")) {
            getCSS(requestUri, out);
        } else if (requestUri.getPath().endsWith(".js")) {
            getJS(requestUri, out);
        } else if (requestUri.getPath().startsWith("/app")) {
            processRequest(requestUri, out);
        } else if (requestUri.getPath().endsWith(".jpeg") || requestUri.getPath().endsWith(".jpg")
                || requestUri.getPath().endsWith(".png") || requestUri.getPath().endsWith(".ico")) {
            try {
                getImage(requestUri, outputStream);
            } catch (IOException ex) {
                System.getLogger(HttpServer.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
        } else {
            notFound(out);
        }
    }

    /**
     * Check if a file exists in the given path
     * @param filePath
     * @return
     */
    private static boolean isFileExists(String filePath) {
        File file = new File(filePath);
        return file.exists() && file.isFile();
    }

    /**
     * handle html responses
     *
     * @param requestUri
     * @param out
     */
    private static void getHTML(URI requestUri, PrintWriter out) {
        String outputLine = "HTTP/1.1 200 OK\n\r"
                + "contente-type: text/html\n\r"
                + "\n\r";

        // create the file path
        String file = requestUri.getPath().equalsIgnoreCase("/") ? basePath + "index.html" : basePath + requestUri.getPath();

        if (!isFileExists(file)) {
            notFound(out);
            return;
        }

        // start reading the file
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String fileLine;
            while ((fileLine = reader.readLine()) != null) {
                outputLine += fileLine + "\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        out.write(outputLine);
    }

    /**
     * hanlde css responses
     *
     * @param requestUri
     * @param out
     */
    private static void getCSS(URI requestUri, PrintWriter out) {
        String outputLine = "HTTP/1.1 200 OK\n\r"
                + "contente-type: text/css\n\r"
                + "\n\r";

        String file = basePath + requestUri.getPath();

        if (!isFileExists(file)) {
            notFound(out);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String fileLine;
            while ((fileLine = reader.readLine()) != null) {
                outputLine += fileLine + "\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        out.write(outputLine);
    }

    /**
     * handle javascript responses
     *
     * @param requestUri
     * @param out
     */
    private static void getJS(URI requestUri, PrintWriter out) {
        String outputLine = "HTTP/1.1 200 OK\n\r"
                + "contente-type: text/javascript\n\r"
                + "\n\r";

        String file = basePath + requestUri.getPath();

        if (!isFileExists(file)) {
            notFound(out);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String fileLine;
            while ((fileLine = reader.readLine()) != null) {
                outputLine += fileLine + "\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        out.write(outputLine);
    }

    /**
     * handle the rest app
     *
     * @param requestUri
     * @param out
     */
    private static void processRequest(URI requestUri, PrintWriter out) {

        String serviceRoute = requestUri.getPath().substring(4);
        Method m = services.get(serviceRoute);

        if (m == null) {
            notFound(out);
            return;
        }

        HttpRequest req = new HttpRequest(requestUri);
        HttpResponse res = new HttpResponse();

        String header = "HTTP/1.1 200 OK\n\r"
                + "contente-type: application/json\n\r"
                + "\n\r";

        try {
            // Obtener todos los parámetros del método
            Parameter[] parameters = m.getParameters();
            Object[] argsValues = new Object[parameters.length];

            // Procesar cada parámetro
            for (int i = 0; i < parameters.length; i++) {
                Parameter param = parameters[i];

                // Verificar si el parámetro tiene la anotación @RequestParam
                if (param.isAnnotationPresent(RequestParam.class)) {
                    RequestParam rp = param.getAnnotation(RequestParam.class);
                    String queryParamName = rp.value();
                    String paramValue = req.getValue(queryParamName);

                    // Si el parámetro está vacío, usar el valor por defecto
                    if (paramValue.equalsIgnoreCase("")) {
                        paramValue = rp.defaultValue();
                    }

                    argsValues[i] = paramValue;
                } else {
                    // Si el parámetro no tiene @RequestParam, usar null o valor por defecto
                    argsValues[i] = null;
                }
            }
            
            out.write(header + m.invoke(null, argsValues));
        } catch (IllegalAccessException ex) {
            System.getLogger(HttpServer.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        } catch (InvocationTargetException ex) {
            System.getLogger(HttpServer.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }

    }

    /**
     * handle image response
     *
     * @param requestUri
     * @param out
     * @throws IOException
     */
    private static void getImage(URI requestUri, OutputStream out) throws IOException {
        String path = requestUri.getPath();
        //File extension
        String fileExtension = path.substring(path.lastIndexOf(".") + 1).toLowerCase();

        String file = path.startsWith("/images/") ? basePath + path : basePath + "images/" + path;

        File realFile = new File(file);

        if (!realFile.exists()) {
            PrintWriter outPrint = new PrintWriter(out, true);
            notFound(outPrint);
            return;
        }

        //response header
        String outputLine = "HTTP/1.1 200 OK\r\n"
                + "Content-Type: image/" + fileExtension + "\r\n"
                + "Content-Length: " + realFile.length() + "\r\n"
                + "\r\n";

        try {
            //Write headers as a text
            out.write(outputLine.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            System.getLogger(HttpServer.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }

        //write content binary of image
        try (FileInputStream fileInputStream = new FileInputStream(realFile); BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream)) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
        out.flush();
    }

    /**
     * handle not found response
     *
     * @param out
     */
    private static void notFound(PrintWriter out) {
        String response = "HTTP/1.1 404 Not Found\r\n"
                + "Content-Type: text/plain\r\n"
                + "\r\n"
                + "404 Not Found";
        out.write(response);
    }


    public static void staticfiles(String staticFile) {
        if (staticFile.startsWith("/")) {
            basePath = "target/classes" + staticFile + "/";
        } else {
            basePath = "target/classes/" + staticFile + "/";
        }

        System.out.println("Static files path set to: " + basePath);

        // create the directory if it does not exist
        Path path = Paths.get(basePath);
        if (!path.toFile().exists()) {
            try {
                path.toFile().mkdirs();
                System.out.println("Static files directory created: " + basePath);
            } catch (Exception e) {
                System.err.println("Could not create static files directory: " + basePath);
                e.printStackTrace();
            }
        }

        //copy the files from src/main/java/resources to current static file path
        copyStaticFiles("src/main/java/resources", basePath);

    }

    public static void copyStaticFiles(String sourceDir, String destDir) {
        Path sourcePath = Paths.get(sourceDir);
        Path destPath = Paths.get(destDir);
        try {
            java.nio.file.Files.walk(sourcePath).forEach(source -> {
                Path destination = destPath.resolve(sourcePath.relativize(source));
                try {
                    if (source.toFile().isDirectory()) {
                        if (!destination.toFile().exists()) {
                            destination.toFile().mkdirs();
                        }
                    } else {
                        java.nio.file.Files.copy(source, destination, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    System.err.println("Could not copy file: " + source.toString());
                    e.printStackTrace();
                }
            });
            System.out.println("Static files copied to: " + destDir);
        } catch (IOException e) {
            System.err.println("Could not copy static files to: " + destDir);
            e.printStackTrace();
        }
    }

}
