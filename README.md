# Uploading Files

This is a Spring Boot web application designed to handle HTTP multi-part file uploads. It allows users to upload files, list uploaded files, and download them using a simple HTML interface.

## Features

- **Upload Files**: Users can upload files via a simple HTML form.
- **List Uploaded Files**: View a list of files already uploaded to the server.
- **Download Files**: Download uploaded files by clicking on the file links.
- **Feedback on Upload**: Displays a success message once the file is uploaded.

---

## Architecture

Below is the UML Class Diagram that illustrates the structure and relationships of the core classes in this application:

```mermaid
classDiagram
    direction LR

    class StorageProperties {
        -String location
        +getLocation() String
        +setLocation(location)
    }

    class StorageException {
        +StorageException(message)
        +StorageException(message, cause)
    }

    class StorageFileNotFoundException {
        +StorageException(message)
        +StorageException(message, cause)
    }

    class StorageService {
        <<interface>>
        +init()
        +store(file)
        +loadAll() Stream<Path>
        +load(filename) Path
        +loadAsResource(filename) Resource
        +deleteAll()
    }

    class FileSystemStorageService {
        -Path rootLocation
        +FileSystemStorageService(properties)
        +store(file)
        +loadAll() Stream
        +load(filename) Path
        +loadAsResource(filename) Resource
        +deleteAll()
        +init()
    }

    class GlobalExceptionHandler {
        +handleStorageFileNotFoundException(e) ResponseEntity
        +handleRuntimeException(e) ResponseEntity
    }

    class FileUploadController {
        -StorageService storageService
        +FileUploadController(storageService)
        +listUploadedFiles(model) String
        +serveFile(filename) ResponseEntity
        +handleFileUpload(file, redirectAttributes) String
    }

    StorageFileNotFoundException --> StorageException : extends
    FileSystemStorageService --> StorageService : implements
    FileSystemStorageService --> StorageProperties : uses
    FileUploadController --> StorageService : uses
    GlobalExceptionHandler --> StorageFileNotFoundException : handles
    GlobalExceptionHandler --> RuntimeException : handles
```
---

## Getting Started

### Prerequisites

Before running this application, ensure you have the following installed:

- **Java 17** or later
- **Maven**
- A modern web browser for testing the HTML interface

## Installing the project

First you must clone the repository.
```bash
# clone repository
$ git clone https://github.com/herikerbeth/uploading-files.git

# enter the project folder
$ cd uploading-files
```

Now, inside IntelliJ, we will install the dependencies with Maven

<img width="300px" src="https://github.com/herikerbeth/assets/blob/main/install-dependencies.png?raw=true">

## Starting
Finally, navigate to the Application class file to run the project.

<img width="300px" src="https://github.com/herikerbeth/assets/blob/main/run-application.png?raw=true">

## Test the Service
With the server running, you need to open a browser and visit http://localhost:8080/ to see the upload form.

## Source

This guide is based on the official Spring documentation from [Spring.io](https://spring.io/guides/gs/uploading-files).