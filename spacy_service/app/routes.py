from flask import request, jsonify
from app import app
import spacy
import pymorphy3

nlp = spacy.load("ru_core_news_sm")
morph = pymorphy3.MorphAnalyzer()


@app.route('/analyze', methods=['POST'])
def analyze_text():
    data = request.json
    text = data.get("text", "")

    doc = nlp(text)
    pos_map = {}
    for token in doc:
        case = morph.parse(token.text)[0]
        if token.pos_ == "NOUN":
            if case.tag.case == "nomn":  # Ищем существительное в именительном падеже
                pos_map.setdefault(token.pos_, []).append(token.text)
        elif token.pos_ == "ADJ":
            pos_map.setdefault(token.pos_, []).append(change_case(token.text, "nomn"))

    response = {
        "pos": pos_map
    }
    return jsonify(response)

def change_case(word, case="gent"):
    parsed_word = morph.parse(word)[0]
    return parsed_word.inflect({case}).word