const fs = require("fs");
const path = require("path");
const http = require("http");

const PORT = 8000;
const SLIDES_DIR = path.join(__dirname, "slides");

// Create HTTP server
const server = http.createServer((req, res) => {
  // Enable CORS
  res.setHeader("Access-Control-Allow-Origin", "*");
  res.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");

  // Handle API endpoint for listing slides
  if (req.url === "/api/slides") {
    fs.readdir(SLIDES_DIR, (err, files) => {
      if (err) {
        res.writeHead(500, { "Content-Type": "application/json" });
        res.end(JSON.stringify({ error: "Failed to read slides directory" }));
        return;
      }

      // Filter for .html files and sort
      const slideFiles = files.filter((file) => file.endsWith(".html")).sort();

      res.writeHead(200, { "Content-Type": "application/json" });
      res.end(JSON.stringify({ slides: slideFiles }));
    });
    return;
  }

  // Serve static files
  let filePath = path.join(__dirname, req.url === "/" ? "index.html" : req.url);
  const extname = path.extname(filePath);

  // Default to index.html for root
  if (req.url === "/" || !extname) {
    filePath = path.join(__dirname, "index.html");
  }

  fs.readFile(filePath, (err, data) => {
    if (err) {
      res.writeHead(404);
      res.end("Not Found");
      return;
    }

    const contentTypes = {
      ".html": "text/html",
      ".css": "text/css",
      ".js": "application/javascript",
      ".json": "application/json",
      ".png": "image/png",
      ".jpg": "image/jpeg",
      ".gif": "image/gif",
      ".svg": "image/svg+xml",
    };

    const contentType = contentTypes[extname] || "text/plain";
    res.writeHead(200, { "Content-Type": contentType });
    res.end(data);
  });
});

server.listen(PORT, () => {
  console.log(`Server running at http://localhost:${PORT}`);
  console.log("Slides API available at http://localhost:${PORT}/api/slides");
});
