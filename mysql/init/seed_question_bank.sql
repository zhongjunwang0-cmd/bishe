USE `english_learning`;

INSERT INTO `biz_question_bank` (`module_type`, `title`, `content`, `difficulty`, `questions_json`, `answers_json`, `status`) VALUES
('READING', 'Urbanization and Sustainability',
'Rapid urbanization presents both tremendous opportunities and formidable challenges for sustainable development. By 2050, an estimated 68% of the world population will reside in urban areas, placing enormous pressure on infrastructure, resources, and ecosystem services. Smart city initiatives leverage digital technologies including IoT sensors, big data analytics, and AI optimization to improve energy efficiency, reduce traffic congestion, and enhance public services.',
'Medium',
'[{"question":"By 2050, what percentage of the world population is expected to live in urban areas?","options":[{"label":"A. 50%","value":"A"},{"label":"B. 68%","value":"B"},{"label":"C. 75%","value":"C"},{"label":"D. 80%","value":"D"}]},{"question":"Which technologies are mentioned as part of smart city initiatives?","options":[{"label":"A. IoT sensors and big data analytics","value":"A"},{"label":"B. Nuclear power and fossil fuels","value":"B"},{"label":"C. Traditional agriculture only","value":"C"},{"label":"D. Manual record keeping","value":"D"}]}]',
'[{"correct":"B","explanation":"The passage explicitly states that 68% of the world population will reside in urban areas by 2050."},{"correct":"A","explanation":"Smart city initiatives use IoT sensors, big data analytics, and AI optimization according to the passage."}]',
'Active'),

('READING', 'Digital Reading Habits',
'The advent of digital media has fundamentally altered reading behaviors across all demographic groups. Research indicates that while overall reading rates have declined, digital reading has significantly increased, particularly among younger generations who prefer news aggregators, social media feeds, and e-books over traditional print media. Cognitive scientists distinguish between deep reading and skimming.',
'Hard',
'[{"question":"What trend does the passage describe regarding digital reading?","options":[{"label":"A. It has significantly increased","value":"A"},{"label":"B. It has completely disappeared","value":"B"},{"label":"C. It is only popular among older adults","value":"C"},{"label":"D. It has replaced all print media","value":"D"}]},{"question":"How do cognitive scientists define deep reading?","options":[{"label":"A. Rapid scanning of online content","value":"A"},{"label":"B. Slow, immersive engagement with complex texts","value":"B"},{"label":"C. Reading only social media feeds","value":"C"},{"label":"D. Skimming headlines quickly","value":"D"}]}]',
'[{"correct":"A","explanation":"The passage states digital reading has significantly increased despite overall reading rate decline."},{"correct":"B","explanation":"Deep reading is defined as slow, immersive engagement that builds critical thinking."}]',
'Active'),

('READING', 'Music and Language Acquisition',
'Emerging research in cognitive neuroscience reveals striking parallels between musical and linguistic processing in the human brain. Both music and language engage overlapping neural networks responsible for rhythm perception, syntactic processing, and working memory. Studies indicate that early musical training significantly enhances phonological awareness, a critical prerequisite for reading acquisition.',
'Medium',
'[{"question":"What neural functions do music and language share according to the passage?","options":[{"label":"A. Rhythm perception and syntactic processing","value":"A"},{"label":"B. Visual processing only","value":"B"},{"label":"C. Motor coordination exclusively","value":"C"},{"label":"D. Long-term memory storage only","value":"D"}]},{"question":"What effect does early musical training have?","options":[{"label":"A. It reduces vocabulary size","value":"A"},{"label":"B. It enhances phonological awareness","value":"B"},{"label":"C. It has no effect on reading","value":"C"},{"label":"D. It replaces language learning","value":"D"}]}]',
'[{"correct":"A","explanation":"Both engage networks for rhythm perception, syntactic processing, and working memory."},{"correct":"B","explanation":"Early musical training significantly enhances phonological awareness, a prerequisite for reading."}]',
'Active');

INSERT INTO `biz_question_bank` (`module_type`, `title`, `content`, `category`, `duration`, `audio_url`, `questions_json`, `answers_json`, `status`) VALUES
('LISTENING', 'Daily English - Software Update', 'M: Have you tried out the new software update they pushed yesterday?\nW: Yes, I have! The interface is so much cleaner and easier to use.',
'Daily Oral', '02:30', 'https://onlinetestcase.com/wp-content/uploads/2023/06/100-KB-MP3.mp3',
'[{"question":"What are the speakers discussing?","options":[{"label":"A. A weekend trip","value":"A"},{"label":"B. A new software program","value":"B"},{"label":"C. A job interview","value":"C"},{"label":"D. Dinner plans","value":"D"}]},{"question":"How does the woman feel about the update?","options":[{"label":"A. She thinks it is confusing","value":"A"},{"label":"B. She loves the new interface","value":"B"},{"label":"C. She has not downloaded it yet","value":"C"},{"label":"D. She wants to revert to the old version","value":"D"}]}]',
'[{"correct":"B","explanation":"The male speaker mentions trying out a new software update.","transcript":"M: Have you tried out the new software update they pushed yesterday?"},{"correct":"B","explanation":"The woman praises the cleaner and easier interface.","transcript":"W: Yes, I have! The interface is so much cleaner and easier to use."}]',
'Active'),

('LISTENING', 'TED Talk - Language and Thought', 'Language is not merely a tool for communication — it fundamentally shapes how we perceive and categorize the world around us. Different languages encode concepts differently, influencing speakers cognitive frameworks.',
'TED Talk', '08:30', 'https://onlinetestcase.com/wp-content/uploads/2023/06/100-KB-MP3.mp3',
'[{"question":"What is the main idea of the talk?","options":[{"label":"A. Language shapes how we think","value":"A"},{"label":"B. All languages are identical","value":"B"},{"label":"C. Translation is impossible","value":"C"},{"label":"D. Grammar rules are universal","value":"D"}]},{"question":"How do different languages differ according to the speaker?","options":[{"label":"A. They encode concepts differently","value":"A"},{"label":"B. They use the same vocabulary","value":"B"},{"label":"C. They have no cultural influence","value":"C"},{"label":"D. They only differ in pronunciation","value":"D"}]}]',
'[{"correct":"A","explanation":"The talk argues language shapes perception and categorization of the world.","transcript":"Language is not merely a tool for communication..."},{"correct":"A","explanation":"Different languages encode concepts differently.","transcript":"Different languages encode concepts differently..."}]',
'Active'),

('LISTENING', 'IELTS - Renting Dialogue', 'Agent: Good morning, I have a two-bedroom flat available near the university.\nStudent: That sounds perfect. What is the monthly rent and does it include utilities?',
'IELTS Mock', '04:15', 'https://onlinetestcase.com/wp-content/uploads/2023/06/100-KB-MP3.mp3',
'[{"question":"What type of accommodation is being discussed?","options":[{"label":"A. A two-bedroom flat","value":"A"},{"label":"B. A studio apartment","value":"B"},{"label":"C. A shared dormitory","value":"C"},{"label":"D. A hotel room","value":"D"}]},{"question":"What does the student want to know?","options":[{"label":"A. The monthly rent and utilities","value":"A"},{"label":"B. The landlord name only","value":"B"},{"label":"C. The building age","value":"C"},{"label":"D. The number of floors","value":"D"}]}]',
'[{"correct":"A","explanation":"The agent offers a two-bedroom flat near the university.","transcript":"I have a two-bedroom flat available near the university."},{"correct":"A","explanation":"The student asks about monthly rent and whether utilities are included.","transcript":"What is the monthly rent and does it include utilities?"}]',
'Active');

INSERT INTO `biz_question_bank` (`module_type`, `title`, `content`, `questions_json`, `answers_json`, `status`) VALUES
('CLOZE', 'Language Learning Basics',
'Learning a foreign language (1)_____ a lot of time and practice. However, it is very rewarding. You can communicate with people from other (2)_____ and understand their culture better.',
'[{"blankIndex":1,"options":["takes","spends","costs","pays"]},{"blankIndex":2,"options":["cities","countries","villages","towns"]}]',
'[{"correct":"takes","explanation":"take time means to spend time; Learning is the subject."},{"correct":"countries","explanation":"Best fits foreign language and culture context."}]',
'Active'),

('CLOZE', 'Smartphones in Daily Life',
'Modern smartphones have become (1)_____ tools in daily life, transforming how people (2)_____, work, and entertain themselves. Research suggests that the (3)_____ use of smartphones is associated with decreased attention spans.',
'[{"blankIndex":1,"options":["indispensable","optional","temporary","minor"]},{"blankIndex":2,"options":["communicate","calculate","negotiate","compete"]},{"blankIndex":3,"options":["excessive","moderate","minimal","occasional"]}]',
'[{"correct":"indispensable","explanation":"Smartphones have become indispensable tools in daily life."},{"correct":"communicate","explanation":"People use phones to communicate, work, and entertain."},{"correct":"excessive","explanation":"Excessive smartphone use is linked to decreased attention spans."}]',
'Active'),

('CLOZE', 'Carbon Neutrality Goals',
'Global efforts to achieve carbon (1)_____ have accelerated in response to increasingly (2)_____ climate change impacts. Governments and corporations are pledging to reduce greenhouse gas (3)_____.',
'[{"blankIndex":1,"options":["neutrality","stability","efficiency","diversity"]},{"blankIndex":2,"options":["severe","minor","temporary","predictable"]},{"blankIndex":3,"options":["emissions","production","consumption","investment"]}]',
'[{"correct":"neutrality","explanation":"carbon neutrality is a fixed collocation."},{"correct":"severe","explanation":"Climate change impacts are increasingly severe."},{"correct":"emissions","explanation":"greenhouse gas emissions is the standard term."}]',
'Active');
