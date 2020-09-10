## Antaeus

Antaeus (/ænˈtiːəs/), in Greek mythology, a giant of Libya, the son of the sea god Poseidon and the Earth goddess Gaia. He compelled all strangers who were passing through the country to wrestle with him. Whenever Antaeus touched the Earth (his mother), his strength was renewed, so that even if thrown to the ground, he was invincible. Heracles, in combat with him, discovered the source of his strength and, lifting him up from Earth, crushed him to death.

Welcome to our challenge.

## The challenge

As most "Software as a Service" (SaaS) companies, Pleo needs to charge a subscription fee every month. Our database contains a few invoices for the different markets in which we operate. Your task is to build the logic that will schedule payment of those invoices on the first of the month. While this may seem simple, there is space for some decisions to be taken and you will be expected to justify them.

## Instructions

Fork this repo with your solution. Ideally, we'd like to see your progression through commits, and don't forget to update the README.md to explain your thought process.

Please let us know how long the challenge takes you. We're not looking for how speedy or lengthy you are. It's just really to give us a clearer idea of what you've produced in the time you decided to take. Feel free to go as big or as small as you want.

## Developing

Requirements:
- \>= Java 11 environment

Open the project using your favorite text editor. If you are using IntelliJ, you can open the `build.gradle.kts` file and it is gonna setup the project in the IDE for you.

### Building

```
./gradlew build
```

### Running

There are 2 options for running Anteus. You either need libsqlite3 or docker. Docker is easier but requires some docker knowledge. We do recommend docker though.

*Running Natively*

Native java with sqlite (requires libsqlite3):

If you use homebrew on MacOS `brew install sqlite`.

```
./gradlew run
```

*Running through docker*

Install docker for your platform

```
docker build -t antaeus
docker run antaeus
```

### App Structure
The code given is structured as follows. Feel free however to modify the structure to fit your needs.
```
├── buildSrc
|  | gradle build scripts and project wide dependency declarations
|  └ src/main/kotlin/utils.kt 
|      Dependencies
|
├── pleo-antaeus-app
|       main() & initialization
|
├── pleo-antaeus-core
|       This is probably where you will introduce most of your new code.
|       Pay attention to the PaymentProvider and BillingService class.
|
├── pleo-antaeus-data
|       Module interfacing with the database. Contains the database 
|       models, mappings and access layer.
|
├── pleo-antaeus-models
|       Definition of the Internal and API models used throughout the
|       application.
|
└── pleo-antaeus-rest
        Entry point for HTTP REST API. This is where the routes are defined.
```

### Main Libraries and dependencies
* [Exposed](https://github.com/JetBrains/Exposed) - DSL for type-safe SQL
* [Javalin](https://javalin.io/) - Simple web framework (for REST)
* [kotlin-logging](https://github.com/MicroUtils/kotlin-logging) - Simple logging framework for Kotlin
* [JUnit 5](https://junit.org/junit5/) - Testing framework
* [Mockk](https://mockk.io/) - Mocking library
* [Sqlite3](https://sqlite.org/index.html) - Database storage engine

Happy hacking 😁!

## Process
Took the first half-day to set up my environment. Personally, this is one of my least favourite parts of developing.
If antaeus was a long-term project, I think I would've spent more time on setting up Docker and my dev environment. 
It was a little annoying having to re-create the Docker image each time to run my new code -had this 
been for work, I probably would've quit by the 2nd week. However, by the second day, I figured my janky Docker setup 
would be sufficient since Test Driven Development is a good way of developing!

After poking around the files and seeing what the important classes contained, it seemed to be that the Payment 
Provider class would deal with the errors such as `CustomerNotFoundException`, `CurrencyMismatchException`, 
so I would not need to code the if statements that would specifically check for these issues.

I realized that I would need a way to access the invoices, so I passed an instance of the `InvoiceService` to 
`BillingService`. Then realized that I would also need access to the database so that the invoice status could 
be updated and persisted in the database.

I allowed the `BillingService` to process just one invoice (based on the id passed in) because I thought this might be 
useful for someone if they are just trying to reprocess a single invoice. Then, I wrote the test to go with this 
situation, and wrote the error situation test. Then, I moved onto the bigger solution which was just fetching all 
pending transactions and processing them similarly. Then, wrote the tests to go with this larger solution. Of course, 
in this scenario, there is more variations of what can occur, so more tests were written.

Once all the tests were written and passing, I went in and manually tested my api endpoints, just to double check.

For the future, I feel that fetching all the pending invoice and then looping through them could have some 
performance issues and it might be better to process them by batches (something similar to pagination, where 
the number of elements processed and the start is kept track of).

Finally, I think it would be nice for the REST Api to not only return a list of modified invoices, but also to 
present the reason for the error. This is why I had multiple catch statements instead of a generation `catch exception`. 
In fact, I think it would be ideal if the `CurrencyMismatchException` and the `NetworkException` could later retry the 
payment: `CurrencyMismatchException` could try to convert the invoice statement to match the customer's currency.

This solution took me just over 2 working days (including set-up time).

P.S. I changed the docker-start.sh file a bit, because just `docker run` wasn't working for me. Hope that is okay!