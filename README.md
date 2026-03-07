# ties - Aerial Mapping System for Pollution managment system *this is mainly for a prototype*

A production-grade distributed pollution management system
- Data acquisition layer (The prototype will be a phone)
- Kafka event streaming pipeline
- Java microservices backend
- Redis caching 
- PostgresSQL relational storage
- MongoDB image object storage
- AI processing tier for frame fusion and map synthesis

## System Architecture
- Data Acquisition Tier - Autonomous drones (but for this prototype we would use Android phones)
- Transportation Tier - Apache Kafka
- Backend Tier - Java microservices
- Analytical Tier - AI frame processing and map generation
- Presentation Tier - Map display

## Services
___________________________________________________________________________________________
| Service                 |               Responsibility                                  |
|-------------------------|---------------------------------------------------------------|
|   Ingestion-service     |        Receives capture blocks from input device              |
|   Validation-service    |              Validates payload integrity                      |
|    Storage-service      |             Manages PostgreSQL and MongoDB                    |
|     Cache-service       |                Manages Redis caching                          |
|  Orchestration-service  |                Coordinates pipeline flow                      |
|_________________________|_______________________________________________________________|

## Getting Started
See docs/ folder for setup instructions and documentations.

## Branch Strategy
- main -- Production only
- develop -- Integration branch
- feature/xxx -- Feature branches (Everyone feature in development has its own)
