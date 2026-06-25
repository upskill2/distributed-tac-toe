# Discussion: Potential Improvements and Alternative Design Approaches

---

## Distributed system design
From my perspective, the task is to assess ability to build distributed system, rather than biz logic or UI layer. Thats why i invested time on fixing inter service communication and building automated CI pipeline (using docker compose)

Not covered in the task, but when building microservices should be considered:
- i choose simple layered architecture for simplicity. But i would rather choose DDD for real world systems 
- distributed cache
- SAGA pattern and rollback/compensation events
- 2PC and Outbox pattern
- in complex systems K8s is the right approach. With kubernetes, there is no need for Spring Cloud LoadBalancer + Eureka entirely
- resiliency & load balancing (i have simple inter-service load balancing just to demonstrate i know the pattern)
- async messaging communications

---

## AI Agents and MCPs.
For sake of the task i didnt build dedicated agents and MCPs. But for my current project i automated development by creating them

---

## 1. E2E Testing
Another complexity for microservices is E2E testing. There are few ways how to design it. 
The good way i see, it has to be built in the dedicated integration env. When we deploy all the apps and send request (in automatic way)
Then verifying output equals to the expected output

---

## 2. CI/CD

Infrastructure is one of the most complex topics in distributed systems. For this task i spent time on making sure and fixing correct service startup ordering
Implemented simply retry (when docker says service is up, but actually its not yet ready, and we need a couple of ms before starting dependent service)

---

## 3. Transactions and concurrency

I have very complex `SessionService.simulate()` method which have both. As this is just a test assignment, i didnt spent much time on debugging and testing the negative cases (rollback) situation. In real world apps, this would have been tested and refactored

---

## 1. N+1 Problem

When we work with RDBMS should always check real SQL fired by the app to understand we do not ddos the db. In my case im using  @ElementCollection which might cause it

---
