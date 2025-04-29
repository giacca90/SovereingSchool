db = db.getSiblingDB("SovSchoolChat");
db.courses_chat.insertOne({ createdAt: new Date() });
db.messages_chat.insertOne({ createdAt: new Date() });
db.users_chat.insertOne({ createdAt: new Date() });

db = db.getSiblingDB("SovSchoolStream");
db.presets.insertOne({ createdAt: new Date() });
db.user_courses.insertOne({ createdAt: new Date() });
