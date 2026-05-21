package com.english.learning.service;

import com.english.learning.entity.Cloze;
import com.english.learning.entity.Listening;
import com.english.learning.entity.Reading;

public interface QuestionGenerateService {
    Reading generateReading();

    Listening generateListening();

    Listening publishListeningFromBank(Long bankId);

    Cloze generateCloze();
}
