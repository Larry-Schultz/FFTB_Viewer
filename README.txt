This is the backend and frontend code for FFTBView.  FFTBView is a website that aggregates and displays data from the stream Final Fantasy Tactics Battlegrounds.  
It collects data from the stream's chat via IRC.  It also collects data from the stream's file server and fftbg.com's tournament API.

The backend is written using Java with the Spring Boot Framework.  Database calls are handled by Hibernate, wrapped with Spring JPA.  HTML files are generated using templates with the Thymeleaf library.

The frontend is written in Javascript using primarily JQuery.  I use the Foundation Framework to handle the styling.