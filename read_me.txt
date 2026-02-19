Cluely - AI Meeting Assistant

Project Overview : 

Cluely is an AI-powered meeting assistant that provides:
- Real-time transcription during live meetings
- Automatic meeting summaries with action items and key decisions
- Audio chunk streaming for continuous recording
- AI-powered analysis using Groq Whisper (speech-to-text) and Groq LLaMA (text analysis)

---

Architecture

Tech Stack

Backend:
- Java 17
- Spring Boot 3.x
- Spring Security + JWT Authentication
- Spring Data JPA + Hibernate
- PostgreSQL Database
- Spring Async + @EnableAsync
- Spring Retry
- WebFlux (WebClient for API calls)
- FFmpeg (audio processing)

AI Services:
- Groq Whisper API (speech-to-text)
- Groq LLaMA 3.3 70B (text analysis)

Frontend (Planned Phase 5):
- Angular
- WebSocket for real-time updates

---

Database Schema

Tables

1. users
```sql
user_id         UUID PRIMARY KEY
email           VARCHAR UNIQUE NOT NULL
password_hash   VARCHAR NOT NULL
first_name      VARCHAR
last_name       VARCHAR
created_at      TIMESTAMP NOT NULL
```

2. meetings
```sql
meeting_id      UUID PRIMARY KEY
user_id         UUID NOT NULL → users(user_id)
title           VARCHAR NOT NULL
source          VARCHAR
status          VARCHAR(20) NOT NULL  -- SCHEDULED, LIVE, PROCESSING, COMPLETED, FAILED, CANCELLED
created_at      TIMESTAMP NOT NULL
started_at      TIMESTAMP
ended_at        TIMESTAMP
deleted         BOOLEAN NOT NULL DEFAULT false
deleted_at      TIMESTAMP
```

Indexes:
- idx_meetings_user on user_id
- idx_meetings_status on status

3. audio_chunks
```sql
chunk_id            UUID PRIMARY KEY
meeting_id          UUID NOT NULL → meetings(meeting_id) ON DELETE CASCADE
sequence_number     INT NOT NULL
file_path           VARCHAR(500) NOT NULL
size_bytes          BIGINT NOT NULL
created_at          TIMESTAMP NOT NULL
status              VARCHAR(20) NOT NULL  -- UPLOADED, PROCESSED, FAILED
mime_type           VARCHAR(50)
duration_ms         INT
deleted             BOOLEAN NOT NULL DEFAULT false
deleted_at          TIMESTAMP

UNIQUE (meeting_id, sequence_number)
```

Indexes:
- idx_audio_chunks_meeting on meeting_id WHERE deleted = false

4. processed_meetings
```sql
processed_meeting_id    UUID PRIMARY KEY
meeting_id              UUID NOT NULL UNIQUE → meetings(meeting_id) ON DELETE CASCADE
full_audio_path         VARCHAR(500)
total_duration_ms       BIGINT
chunk_count             INT NOT NULL
processing_status       VARCHAR(20) NOT NULL  -- PENDING, IN_PROGRESS, COMPLETED, FAILED
processed_at            TIMESTAMP NOT NULL
file_size_bytes         BIGINT
error_message           VARCHAR(1000)
```

Indexes:
- idx_processed_meetings_meeting on meeting_id
- idx_processed_meetings_status on processing_status

5. transcripts
```sql
transcript_id       UUID PRIMARY KEY
meeting_id          UUID NOT NULL → meetings(meeting_id)
speaker             VARCHAR(20) NOT NULL
text                TEXT NOT NULL
start_time          DOUBLE PRECISION
end_time            DOUBLE PRECISION
confidence          DOUBLE PRECISION
created_at          TIMESTAMP NOT NULL
deleted             BOOLEAN NOT NULL DEFAULT false
deleted_at          TIMESTAMP
```

Indexes:
- idx_transcripts_meeting on meeting_id WHERE deleted = false

6. notes
```sql
note_id         UUID PRIMARY KEY
user_id         UUID NOT NULL → users(user_id)
meeting_id      UUID NOT NULL → meetings(meeting_id)
content         TEXT NOT NULL
created_at      TIMESTAMP NOT NULL
updated_at      TIMESTAMP
deleted         BOOLEAN NOT NULL DEFAULT false
deleted_at      TIMESTAMP
```

Indexes:
- idx_notes_user on user_id
- idx_notes_meeting on meeting_id

---

Authentication

JWT Token Authentication

Login Endpoint:
```
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}

Response:
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "userId": "uuid",
  "email": "user@example.com"
}
```

Using JWT Token:
All authenticated endpoints require:
```
Authorization: Bearer {token}
```

Token extracted using `SecurityUtils.getCurrentUserId()` which reads from `SecurityContextHolder`.

---

API Endpoints

Authentication

Login
```
POST /api/auth/login
Body: { "email": "...", "password": "..." }
Response: { "token": "...", "userId": "..." }
```

Register
```
POST /api/users
Body: { "email": "...", "password": "...", "firstName": "...", "lastName": "..." }
Response: 201 Created
```

---

Meetings

Create Meeting
```
POST /meetings
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "Q3 Budget Meeting",
  "source": "web",
  "userId": "uuid"
}

Response: 201 Created
{
  "meetingId": "uuid",
  "userId": "uuid",
  "title": "Q3 Budget Meeting",
  "status": "SCHEDULED",
  "createdAt": "2026-02-17T10:30:00"
}
```

Start Meeting (Set to LIVE)
```
POST /meetings/{meetingId}/start
Authorization: Bearer {token}

Response: 200 OK
{
  "meetingId": "uuid",
  "status": "LIVE",
  "startedAt": "2026-02-17T10:31:00"
}
```

End Meeting
```
POST /meetings/{meetingId}/end
Authorization: Bearer {token}

Response: 204 No Content
```
Note: Triggers async pipeline → chunk assembly → transcription → AI analysis

Get Meeting
```
GET /meetings/{meetingId}
Authorization: Bearer {token}

Response: 200 OK
{
  "meetingId": "uuid",
  "title": "...",
  "status": "COMPLETED",
  "startedAt": "...",
  "endedAt": "..."
}
```

List Meetings
```
GET /meetings?page=0&size=20
Authorization: Bearer {token}

Response: 200 OK
{
  "content": [ /* meetings */ ],
  "totalElements": 50,
  "totalPages": 3
}
```

---

Audio Chunks

Upload Audio Chunk
```
POST /api/meetings/{meetingId}/chunks
Authorization: Bearer {token}
Content-Type: multipart/form-data

Form Data:
- sequenceNumber: 1
- audioFile: [binary audio file]

Response: 201 Created
{
  "chunkId": "uuid",
  "meetingId": "uuid",
  "sequenceNumber": 1,
  "sizeBytes": 209396,
  "status": "UPLOADED",
  "mimeType": "audio/mpeg",
  "createdAt": "2026-02-17T10:32:00"
}
```

Notes:
- Meeting must be in `LIVE` status
- Sequence numbers must be unique per meeting
- Triggers async transcription immediately
- Estimated duration calculated from file size

Get Chunks for Meeting
```
GET /api/meetings/{meetingId}/chunks
Authorization: Bearer {token}

Response: 200 OK
[
  {
    "chunkId": "uuid",
    "sequenceNumber": 1,
    "sizeBytes": 209396,
    "status": "PROCESSED",
    "createdAt": "..."
  },
  { ... }
]
```

Get Upload Progress
```
GET /api/meetings/{meetingId}/chunks/progress
Authorization: Bearer {token}

Response: 200 OK
{
  "totalChunks": 15,
  "lastSequenceNumber": 15,
  "meetingStatus": "LIVE"
}
```

---

Transcripts

Get Meeting Transcript
```
GET /transcripts?meetingId={meetingId}&page=0&size=100
Authorization: Bearer {token}

Response: 200 OK
{
  "content": [
    {
      "transcriptId": "uuid",
      "meetingId": "uuid",
      "speaker": "Unknown",
      "text": "The team discussed Q3 budget...",
      "startTime": 0.0,
      "endTime": 13.5,
      "confidence": 0.92,
      "createdAt": "2026-02-17T10:35:00"
    },
    { ... }
  ]
}
```

---

Notes (AI-Generated)

Get Meeting Notes
```
GET /notes?meetingId={meetingId}&page=0&size=50
Authorization: Bearer {token}

Response: 200 OK
{
  "content": [
    {
      "noteId": "uuid",
      "meetingId": "uuid",
      "content": "SUMMARY: The team discussed Q3 marketing budget...",
      "createdAt": "2026-02-17T10:40:00"
    },
    {
      "noteId": "uuid",
      "content": "ACTION ITEM: John to send budget report by Friday",
      "createdAt": "2026-02-17T10:40:01"
    },
    {
      "noteId": "uuid",
      "content": "DECISION: Approved $50K marketing budget increase",
      "createdAt": "2026-02-17T10:40:02"
    }
  ]
}
```

Create Manual Note
```
POST /notes
Authorization: Bearer {token}
Content-Type: application/json

{
  "meetingId": "uuid",
  "content": "Follow up with Sarah about slides"
}

Response: 201 Created
```

Update Note
```
PUT /notes/{noteId}
Authorization: Bearer {token}

{
  "content": "Updated note content"
}

Response: 200 OK
```

Delete Note (Soft Delete)
```
DELETE /notes/{noteId}
Authorization: Bearer {token}

Response: 204 No Content
```

---

Async Processing Pipelines

Real-Time Pipeline (During Meeting)

```
1. User uploads audio chunk
   ↓
2. AudioChunkService.uploadChunk()
   - Validates meeting is LIVE
   - Stores file to disk
   - Saves metadata to DB
   - Returns immediately to user
   ↓
3. TranscriptionService.transcribeChunkAsync() [ASYNC]
   - Sends chunk to Groq Whisper API
   - Receives transcribed text
   - Calculates time offset based on sequence
   - Saves transcript fragment to DB
   - Updates chunk status → PROCESSED
```

Latency: ~3-5 seconds from upload to transcript saved

---

Post-Meeting Pipeline (After Meeting Ends)

```
1. User ends meeting
   ↓
2. MeetingService.endMeeting()
   - Sets status → PROCESSING
   - Triggers ChunkProcessingService.processMeetingChunksAsync()
   ↓
3. ChunkProcessingService [ASYNC]
   - Validates chunk sequence (no gaps)
   - Calls ChunkAssemblyService
   ↓
4. ChunkAssemblyService.assembleChunks() [ASYNC]
   - Creates FFmpeg concat file
   - Combines all chunks into single MP3
   - Saves to /output directory
   - Saves ProcessedMeeting record
   - Triggers TranscriptionService
   ↓
5. TranscriptionService.transcribeFullAudioAsync() [ASYNC]
   - Sends full audio to Groq Whisper API
   - Receives complete transcript
   - Saves to DB
   - Triggers MeetingAnalysisService
   ↓
6. MeetingAnalysisService.analyzeMeetingAsync() [ASYNC]
   - Loads all transcript fragments
   - Assembles into readable format:
     "[Unknown]: Hello team..."
   - Sends to Groq LLaMA API
   - Receives: summary, action items, decisions, key topics
   ↓
7. AnalysisMapper
   - Maps AI response to Note entities:
     - 1 note for summary
     - 1 note per action item
     - 1 note per decision
     - 1 note for key topics
   ↓
8. NoteRepository.saveAll()
   - Saves all notes to DB
   ↓
9. Meeting status → COMPLETED
```

Total Time: ~10-20 seconds for a 5-minute meeting

---

AI Integration

Speech-to-Text (Groq Whisper)

API: `https://api.groq.com/openai/v1/audio/transcriptions`

Model: `whisper-large-v3-turbo`

Request:
```
POST /audio/transcriptions
Content-Type: multipart/form-data

- file: audio.mp3
- model: whisper-large-v3-turbo
- language: en
- response_format: verbose_json
```

Response:
```json
{
  "text": "The team discussed Q3 budget",
  "language": "english",
  "segments": [
    {
      "start": 0.0,
      "end": 2.5,
      "text": "The team discussed",
      "avg_logprob": -0.15
    }
  ]
}
```

Retry Logic:
- 3 attempts with exponential backoff (1s, 2s, 4s)
- If all fail → chunk marked as FAILED

---

Text Analysis (Groq LLaMA)

API: `https://api.groq.com/openai/v1/chat/completions`

Model: `llama-3.3-70b-versatile`

**Request:**
```json
{
  "model": "llama-3.3-70b-versatile",
  "messages": [
    {
      "role": "system",
      "content": "You are an expert meeting analyst..."
    },
    {
      "role": "user",
      "content": "Analyze this transcript: [transcript text]"
    }
  ],
  "temperature": 0.3,
  "max_tokens": 1000,
  "response_format": { "type": "json_object" }
}
```

**Response:**
```json
{
  "choices": [{
    "message": {
      "content": "{
        \"summary\": \"The team discussed Q3 marketing budget...\",
        \"actionItems\": [
          \"John to send budget report by Friday\",
          \"Sarah to schedule follow-up meeting\"
        ],
        \"decisions\": [
          \"Approved $50K marketing budget increase\"
        ],
        \"keyTopics\": [\"budget\", \"marketing\", \"Q3\"],
        \"sentiment\": \"POSITIVE\"
      }"
    }
  }]
}
```

Retry Logic:
- 3 attempts with exponential backoff
- If all fail → no notes generated, logged as error

---

File Storage

Audio Chunks
Location: `C:/Users/Lenovo_Owner/cluely/audio-chunks/`

Naming: `meeting_{meetingId}_chunk_{sequenceNumber}.mp3`

Example: `meeting_abc-123_chunk_0001.mp3`

Assembled Audio
Location: `C:/Users/Lenovo_Owner/cluely/output/`

Naming: `meeting_{meetingId}_full.mp3`

Example: `meeting_abc-123_full.mp3`

---

Configuration

application.properties

```properties
Database
spring.datasource.url=jdbc:postgresql://localhost:5432/cluely_db
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

File Upload
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

Audio Storage
cluely.storage.audio-chunks-path=C:/Users/Lenovo_Owner/cluely/audio-chunks
cluely.storage.output-path=C:/Users/Lenovo_Owner/cluely/output
cluely.storage.max-chunk-size=10485760

FFmpeg
ffmpeg.path=C:/ffmpeg/bin/ffmpeg.exe
ffprobe.path=C:/ffmpeg/bin/ffprobe.exe

Groq API
groq.api.key=gsk_your_key_here
groq.api.url=https://api.groq.com/openai/v1/audio/transcriptions
groq.whisper.model=whisper-large-v3-turbo
groq.llm.url=https://api.groq.com/openai/v1/chat/completions
groq.llm.model=llama-3.3-70b-versatile

AI Providers
cluely.ai.speech-provider=groq
cluely.ai.analysis-provider=groq

JWT
jwt.secret=your-256-bit-secret-key-here
jwt.expiration=86400000

Async
spring.task.execution.pool.core-size=5
spring.task.execution.pool.max-size=10
```

---

Setup Instructions

Prerequisites

1. Java 17+
2. PostgreSQL 14+
3. Maven 3.8+
4. FFmpeg installed at `C:/ffmpeg/bin/`
5. Groq API Key from https://console.groq.com

Database Setup

```sql
CREATE DATABASE cluely_db;
CREATE USER cluely_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE cluely_db TO cluely_user;
```

Run Migrations

Migrations are in `src/main/resources/db/migration/`

Using Flyway (auto-runs on startup):
```
V1__initial_schema.sql
V2__add_jwt_users.sql
V3__add_audio_chunks.sql
V4__add_processed_meetings.sql
```

Build & Run

```bash
# Clone repository
git clone <repo-url>
cd cluely-api

# Configure application.properties
# Add your database credentials and Groq API key

# Build
mvn clean install

# Run
mvn spring-boot:run

# Or run JAR
java -jar target/cluely-api-1.0.0.jar
```

**Application starts on:** `http://localhost:8080`

---

Testing with Postman

1. Register User
```
POST http://localhost:8080/api/auth/register
{
  "email": "test@example.com",
  "password": "password123",
  "firstName": "Test",
  "lastName": "User"
}
```

2. Login
```
POST http://localhost:8080/api/auth/login
{
  "email": "test@example.com",
  "password": "password123"
}

→ Copy JWT token from response
```

3. Create Meeting
```
POST http://localhost:8080/meetings
Authorization: Bearer {token}
{
  "title": "Test Meeting",
  "source": "postman",
  "UserId" : "uuid"
}

→ Copy meetingId
```

4. Start Meeting
```
POST http://localhost:8080/meetings/{meetingId}/start
Authorization: Bearer {token}
```

5. Upload Audio Chunks
```
POST http://localhost:8080/api/meetings/{meetingId}/chunks
Authorization: Bearer {token}
Content-Type: multipart/form-data

Form Data:
- sequenceNumber: 1
- audioFile: [select audio file]

Repeat with sequenceNumber: 2, 3, etc.
```

6. End Meeting
```
POST http://localhost:8080/meetings/{meetingId}/end
Authorization: Bearer {token}
```

7. Wait 10-15 seconds, then check results

Check Transcripts:
```
GET http://localhost:8080/transcripts?meetingId={meetingId}
Authorization: Bearer {token}
```

Check AI-Generated Notes:
```
GET http://localhost:8080/notes?meetingId={meetingId}
Authorization: Bearer {token}
```

---

## 📦 Project Structure

```
src/main/java/com/cluely/
├── CluelyApiApplication.java (@EnableAsync, @EnableRetry)
├── security/
│   ├── SecurityConfig.java
│   ├── JwtTokenProvider.java
│   └── SecurityUtils.java
├── user/
│   ├── entity/UserEntity.java
│   ├── repository/UserRepository.java
│   └── service/UserService.java
├── meeting/
│   ├── entity/
│   │   ├── MeetingEntity.java
│   │   └── MeetingStatus.java
│   ├── repository/MeetingRepository.java
│   ├── service/MeetingService.java
│   └── controller/MeetingController.java
├── audio_chunks/
│   ├── entity/
│   │   ├── AudioChunkEntity.java
│   │   └── ChunkStatus.java
│   ├── repository/AudioChunkRepository.java
│   ├── service/
│   │   ├── AudioChunkService.java
│   │   ├── FileStorageService.java
│   │   └── ChunkProcessingService.java
│   ├── controller/AudioChunkController.java
│   ├── dto/
│   │   ├── AudioChunkResponseDTO.java
│   │   └── ChunkProgressResponseDTO.java
│   └── mapper/AudioChunkMapper.java
├── meeting_processing/
│   ├── entity/
│   │   ├── ProcessedMeetingEntity.java
│   │   └── ProcessingStatus.java
│   ├── repository/ProcessedMeetingRepository.java
│   └── service/ChunkAssemblyService.java
├── transcript/
│   ├── entity/TranscriptEntity.java
│   ├── repository/TranscriptRepository.java
│   ├── service/TranscriptService.java
│   └── mapper/TranscriptMapper.java
├── note/
│   ├── entity/NoteEntity.java
│   ├── repository/NoteRepository.java
│   ├── service/NoteService.java
│   └── controller/NoteController.java
├── ai/
│   ├── config/AIConfig.java
│   ├── speech/
│   │   ├── SpeechToTextService.java (interface)
│   │   ├── TranscriptionService.java (orchestrator)
│   │   ├── groq/GroqWhisperService.java
│   │   ├── dto/
│   │   │   ├── TranscriptionRequestDTO.java
│   │   │   └── TranscriptionResponseDTO.java
│   │   ├── mapper/TranscriptionMapper.java
│   │   └── exception/TranscriptionException.java
│   └── analysis/
│       ├── AIAnalysisService.java (interface)
│       ├── MeetingAnalysisService.java (orchestrator)
│       ├── groq/GroqAnalysisService.java
│       ├── dto/
│       │   ├── AnalysisRequestDTO.java
│       │   └── AnalysisResponseDTO.java
│       ├── mapper/AnalysisMapper.java
│       └── exception/AnalysisException.java
└── global/
    ├── GlobalExceptionHandler.java
    └── NotFoundException.java
```

---

Completed Features (Phases 1-3)

Phase 1: Core CRUD Operations
- User management
- Meeting CRUD
- Note CRUD
- Transcript CRUD

✅ Phase 2: Security & JWT
- JWT token authentication
- User registration/login
- Ownership enforcement (users only see their data)

✅ Phase 2.1: Dashboard Aggregation
- Paginated meeting list
- Meeting with notes/transcripts

✅ Phase 2.2.1: Audio Chunk Ingestion
- Upload chunks during live meeting
- Store on disk with metadata
- Duplicate prevention
- Status validation

✅ Phase 2.2.2: Chunk Assembly
- FFmpeg integration
- Combine chunks into full audio
- Store assembled file

✅ Phase 3: AI Intelligence Layer
- Groq Whisper integration (speech-to-text)
- Real-time chunk transcription
- Full audio transcription
- Groq LLaMA integration (text analysis)
- Automatic summary generation
- Action item extraction
- Decision detection
- AI provider abstraction (swap Groq ↔ OpenAI)
- Retry logic with exponential backoff

---

Planned Features (Phases 4-6)

Phase 4: Real-Time AI Assistance (Next)
- WebSocket infrastructure
- Live transcription streaming
- Real-time AI suggestions during meetings
- Context-aware AI (remembers conversation)
- Knowledge base integration (search past notes/docs)
- Question answering during meetings

Phase 5: Frontend (Angular)
- Meeting recording interface
- Audio capture from browser
- Real-time suggestions panel
- Live captions display
- Post-meeting dashboard
- Transcript viewer
- Note editor

Phase 6: Production Readiness
- Performance optimization
- Caching strategy
- Rate limiting
- Security hardening
- Monitoring & logging
- Docker containerization
- CI/CD pipeline
- Cloud deployment (AWS/GCP)

---

Error Handling

Common Errors

401 Unauthorized
- JWT token missing or expired
- Solution: Login again to get new token

400 Bad Request - Invalid Meeting State
```json
{
  "error": "INVALID_MEETING_STATE",
  "message": "Meeting must be LIVE to accept chunks"
}
```
- Solution: Call `/meetings/{id}/start` first

409 Conflict - Duplicate Chunk
```json
{
  "error": "DUPLICATE_CHUNK",
  "message": "Chunk with sequence number 1 already exists"
}
```
- Solution: Use unique sequence numbers per meeting

500 Internal Server Error - Transcription Failed
```json
{
  "error": "SERVER_ERROR",
  "message": "Transcription failed after 3 attempts"
}
```
- Check Groq API key validity
- Check network connectivity
- Check server logs for details

413 Payload Too Large
- File exceeds 10MB limit
- Solution: Use smaller chunks or increase limit in properties

---

Monitoring & Logs

Key Log Messages

Successful chunk upload:
```
Starting transcription for chunk: abc-123
Saved transcript fragment for chunk: abc-123
Chunk abc-123 marked as PROCESSED
```

Successful meeting processing:
```
Starting assembly for meeting: xyz-789
Successfully assembled meeting: xyz-789
Starting full audio transcription for meeting: xyz-789
Saved full transcript for meeting: xyz-789
Starting meeting analysis for: xyz-789
AI analysis completed for meeting: xyz-789
Saved 5 notes for meeting: xyz-789
```

Retry attempts:
```
WARN: Groq API call failed, retrying (attempt 1/3)...
WARN: Groq API call failed, retrying (attempt 2/3)...
ERROR: All 3 transcription attempts failed for meeting: xyz-789
```

Enable Debug Logging

```properties
logging.level.com.cluely=DEBUG
logging.level.org.hibernate.SQL=DEBUG
```

---

Contributing

Code Style
- Follow existing package structure
- Use DTOs for API layer
- Entities never exposed directly to controllers
- Mappers convert DTO ↔ Entity
- Async for all long-running operations
- Proper exception handling with custom exceptions

Testing
- Unit tests for service layer
- Integration tests for API endpoints
- Manual testing via Postman

---

License

Proprietary - All rights reserved

---

Team

Backend Developer: [Varun Chaturvedi]
Architecture: Dual Pipeline (Real-time + Post-meeting)
AI Integration: Groq Whisper + Groq LLaMA

---

Support

For issues or questions:
- Check logs in console
- Verify Groq API key is valid
- Ensure FFmpeg is installed correctly
- Check database permissions

---

Current Version: 3.6 (AI Intelligence Layer with Retry Logic Complete)

Last Updated: February 17, 2026