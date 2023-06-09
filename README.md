# Spring-MVC-Project

The project is a Spring MVC based system that aims to manage a database of musicians and their music, as well as the managers who work with them. The architecture of the project follows the Model-View-Controller pattern, which separates the application into three layers: the Model layer, which contains the business logic and the code that allows access to data; the View layer, responsible for rendering the user interface; and the Controller, which handles user requests and sends responses back to the client.

There are 8 entities in the application, namely Address, Album, Artist, Consult, Deals, Manager, RecordLabel, and Song. All these entities represent tables in the database and are used to store and retrieve data. They are linked together by various relationships, such as one-to-one, one-to-many, many-to-many, and many-to-one. For example, an artist can have multiple albums and songs, and an album can have multiple songs. A manager can have multiple artists, and an artist can have multiple consultations/meetings and deals with a manager. Multiple artists can belong to a Record Label, which can also have multiple managers working within it.

As for the database, two different databases were used, and two distinct profiles were created. The testing profile uses the in-memory H2 database, which allows for quick and easy testing of the application. In contrast, the production profile uses the MySQL Workbench database, which can handle large volumes of data.

The security of the application is implemented using Spring Security. Password encryption to securely store user passwords in the database was performed using the PasswordEncoder interface. The application has two roles: ROLE_ADMIN and ROLE_MANAGER. The ROLE_ADMIN role has the highest level of access and can perform any action within the application. The ROLE_MANAGER role has a lower level of access and can only perform specific actions. To secure web resources, the application uses URL-based Security configuration. Certain URL addresses are restricted to certain roles, allowing only users with the necessary authority to access those resources.


The project architecture also includes the frontend part, which interacts with the backend. The interface is built using Thymeleaf, which allows for the creation of dynamic web pages.
