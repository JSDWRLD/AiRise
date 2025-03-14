# .NET Web API with MongoDB Backend

## Prerequisites

### For Mac
- .NET SDK (Install via [dotnet website](https://dotnet.microsoft.com/download))
- MongoDB Community Server (Install via [MongoDB website](https://www.mongodb.com/try/download/community))

### For Windows
- .NET SDK (Install via [dotnet website](https://dotnet.microsoft.com/download))
- MongoDB Community Server (Install via [MongoDB website](https://www.mongodb.com/try/download/community))

## Project Setup

### 1. Clone the Repository
```bash
git clone https://github.com/JSDWRLD/AiRise
cd Backend
```

### 2. Install MongoDB Driver

The MongoDB driver is already added to the project via the `.csproj` file.
However, if needed, you can manually add it with the following command:

```bash
dotnet add package MongoDB.Driver
```

> **Note:** This step is only needed if the dependency is not already present in the `.csproj` file.

### 3. Restore Packages
```bash
dotnet restore
```

### 4. Run the Application
```bash
dotnet run
```

## MongoDB Connection String
Make sure your `appsettings.json` has the correct connection string:

```json
{
  "ConnectionStrings": {
    "MongoDB": "mongodb://localhost:27017/your-database-name"
  }
}
```

## Development Environment

### For Local Development

- MongoDB must be running locally
- API will run on `https://localhost:5001` (or the port specified in your `launchSettings.json`)

## Additional Commands

### To Add a New Package
```bash
dotnet add package <package-name>
```

### To Update Dependencies
```bash
dotnet restore
```

## Useful Resources
- Official MongoDB C# Driver Docs: https://www.mongodb.com/docs/drivers/csharp/
- .NET Documentation: https://learn.microsoft.com/en-us/dotnet/

