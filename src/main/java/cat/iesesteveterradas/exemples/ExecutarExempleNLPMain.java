package cat.iesesteveterradas.exemples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;

import edu.stanford.nlp.ling.CoreAnnotations.*;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;


public class ExecutarExempleNLPMain {
    private static final Logger logger = LoggerFactory.getLogger(ExecutarExempleNLPMain.class);

    public static void main(String[] args) throws Exception {
        String text = "John Doe, a software engineer at Google, recently visited New York City. He said, \"It's an amazing place!\" The trip made him feel very happy.";

        String basePath = System.getProperty("user.dir") + "/data/models/";

        // Updated paths for the provided model files
        InputStream modelInSentence = new FileInputStream(basePath + "opennlp-en-ud-ewt-sentence-1.0-1.9.3.bin");
        InputStream modelInToken = new FileInputStream(basePath + "opennlp-en-ud-ewt-tokens-1.0-1.9.3.bin");
        InputStream modelInPOS = new FileInputStream(basePath + "en-pos-maxent.bin");
        InputStream modelInPerson = new FileInputStream(basePath + "en-ner-person.bin");

        // Sentence detection
        SentenceModel modelSentence = new SentenceModel(modelInSentence);
        SentenceDetectorME sentenceDetector = new SentenceDetectorME(modelSentence);
        String[] sentences = sentenceDetector.sentDetect(text);
        logger.info("Sentence Detection:");
        Arrays.stream(sentences).forEach(sentence -> logger.info(sentence));

        // Tokenization
        TokenizerModel modelToken = new TokenizerModel(modelInToken);
        TokenizerME tokenizer = new TokenizerME(modelToken);
        logger.info("\nTokenization and POS Tagging:");
        for (String sentence : sentences) {
            try{
                String[] tokens = tokenizer.tokenize(sentence);
            
                // POS Tagging
                POSModel modelPOS = new POSModel(modelInPOS);
                POSTaggerME posTagger = new POSTaggerME(modelPOS);
                String[] tags = posTagger.tag(tokens);
                
                for (int i = 0; i < tokens.length; i++) {
                    logger.info(tokens[i] + " (" + tags[i] + ")");
                }
            } catch (Exception e){
                logger.error(e.getMessage());
            }
        }

        // Named Entity Recognition
        TokenNameFinderModel modelPerson = new TokenNameFinderModel(modelInPerson);
        NameFinderME nameFinder = new NameFinderME(modelPerson);
        logger.info("\nNamed Entity Recognition:");
        for (String sentence : sentences) {
            String[] tokens = tokenizer.tokenize(sentence);
            opennlp.tools.util.Span[] nameSpans = nameFinder.find(tokens);
            for (opennlp.tools.util.Span s : nameSpans) {
                logger.info("Entity: " + tokens[s.getStart()]);
            }
        }

        // Clean up IO resources
        modelInSentence.close();
        modelInToken.close();
        modelInPOS.close();
        modelInPerson.close();


        // Inicialitza Stanford CoreNLP
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, sentiment");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // Crea un document amb el text
        Annotation document = new Annotation(text);
        pipeline.annotate(document);

        ///// 
        boolean firstName = false;

        // Obté les frases del document
        List<CoreMap> sentencesList2 = document.get(SentencesAnnotation.class);

        for (CoreMap sentence : sentencesList2) {
            // Mostra tokens i etiquetes POS de cada frase
            for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
                String word = token.get(TextAnnotation.class);
                String pos = token.get(PartOfSpeechAnnotation.class);
                logger.info(word + " (" + pos + ")");
            }
            
            // Mostra el reconeixement d'entitats anomenades
            for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
                String word = token.get(TextAnnotation.class);
                String ne = token.get(NamedEntityTagAnnotation.class);
                logger.info("Entity: " + word + " (" + ne + ")");
            }

            /////
            String name = ""; 
            
            // Reconeixement de Named Entity Recognition (NER)
            for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
                String word = token.getString(TextAnnotation.class);
                String ner = token.getString(NamedEntityTagAnnotation.class);
                
                if ("PERSON".equals(ner)) {
                    if (!firstName) {
                        name = word;
                        firstName = true;
                    } else {
                        String mssg = "Entity Detected: " + name + " " + word + " - Entity Type: " + ner;
                        logger.info(mssg);
                        writeToFile(mssg);
                        firstName = false;
                    }
                } 
                // Comprova si el token és una entitat anomenada (NER)
                else if (!"O".equals(ner)) { // Ignora els tokens que no són entitats (etiquetats com 'O')
                    logger.info("Entity Detected: " + word + " - Entity Type: " + ner);
                } 
            }

            // Anàlisi de sentiments
            String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
            logger.info("Sentiment: " + sentiment);
        }        
    }

    public static void writeToFile(String line) {
        String filePath = "./data/noms_propis.txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write(line);
            writer.newLine(); 
        } catch (IOException e) {
            System.err.println("Error writing to the file: " + e.getMessage());
        }
    }
}
