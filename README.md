<a id="readme-top"></a>

<br />
<div align="center">
   <a href="https://github.com/alejandraoshea/sma-server">
     <img src="images/logo.png" alt="SMA Server Logo" width="80" height="80">
   </a>
  <h3 align="center">SMA Server</h3>

  <p align="center">
    Backend server for the SMA Telemedicine Platform. Handles patients, doctors, signals (ECG/EMG), and session management.
    <br />
    <a href="https://github.com/alejandraoshea/sma-server"><strong>Explore the docs »</strong></a>
    <br />
    <br />
    <a href="https://github.com/alejandraoshea/sma-server">View Demo</a>
    &middot;
    <a href="https://github.com/alejandraoshea/sma-server/issues/new?labels=bug">Report Bug</a>
    &middot;
    <a href="https://github.com/alejandraoshea/sma-server/issues/new?labels=enhancement">Request Feature</a>
  </p>
</div>

<details>
  <summary>Table of Contents</summary>
  <ol>
    <li><a href="#about-the-project">About The Project</a></li>
    <li><a href="#built-with">Built With</a></li>
    <li><a href="#getting-started">Getting Started</a></li>
    <li><a href="#usage">Usage</a></li>
    <li><a href="#roadmap">Roadmap</a></li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
    <li><a href="#acknowledgments">Acknowledgments</a></li>
  </ol>
</details>

## About The Project

SMA Server is the backend service for a telemedicine platform for SMA (Spinal Muscular Atrophy) patients. It supports:

- Patient and doctor management
- ECG and EMG signal storage and processing
- Measurement session tracking
- CSV summary generation for sessions
- Reporting system for doctors

<p align="right">(<a href="#readme-top">back to top</a>)</p>

### Built With

* Java
* Spring Boot
* PostgreSQL

<p align="right">(<a href="#readme-top">back to top</a>)</p>

## Getting Started

Follow these steps to set up a local development environment.

### Prerequisites

- Java 17+
- Maven
- PostgreSQL database

### Installation

1. Clone the repository:
   ```sh
   git clone https://github.com/alejandraoshea/sma-server.git
   cd sma-server

2. Configure your database in application.properties:
   ```sh
   spring.datasource.url=jdbc:postgresql://localhost:5432/sma_db
   spring.datasource.username=your_db_user
   spring.datasource.password=your_db_password

3. Build and run the project:
   ```sh
   mvn clean install
   mvn spring-boot:run

<p align="right">(<a href="#readme-top">back to top</a>)</p>


Usage
• API endpoints for patients, doctors, sessions, and signals are exposed via REST.
• Upload ECG/EMG files to a session to store signals.
• Generate CSV summaries for measurement sessions.

For detailed API usage, refer to the /docs folder (if available) or Swagger UI.

<p align="right">(<a href="#readme-top">back to top</a>)</p>


Roadmap
• Add authentication & authorization (JWT)
• Implement advanced signal analytics
• Integrate notification system for doctor approvals
• Expand reporting system

See the open issues￼ for full details.

<p align="right">(<a href="#readme-top">back to top</a>)</p>


Contributing

Contributions make the project better!

1. Fork the repo
2. Create your feature branch (git checkout -b feature/AmazingFeature)
3. Commit your changes (git commit -m 'Add some AmazingFeature')
4. Push to the branch (git push origin feature/AmazingFeature)
5. Open a Pull Request

Top contributors:

<a href="https://github.com/alejandraoshea/sma-server/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=alejandraoshea/sma-server" alt="contrib.rocks image" />
</a>


<p align="right">(<a href="#readme-top">back to top</a>)</p>


License

Distributed under the MIT License. See LICENSE.txt for more information.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<p align="right">(<a href="#readme-top">back to top</a>)</p>


Acknowledgments
• Best README Template￼
• Contrib.rocks for contributors graph￼
• Spring Boot Documentation￼

<p align="right">(<a href="#readme-top">back to top</a>)</p>


<!-- MARKDOWN LINKS & IMAGES -->


---
