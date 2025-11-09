import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.rag.query.router.QueryRouter;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class TestPasDeRag {

    public static void main(String[] args) throws Exception {
        String cle = System.getenv("GEMINI_KEY");

        ChatModel model = GoogleAiGeminiChatModel.builder()
                .apiKey(cle)
                .modelName("gemini-2.0-flash-exp")
                .temperature(0.3)
                .logRequestsAndResponses(true)
                .build();

        EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

        URL fileUrl = TestRoutage.class.getResource("LLM_Course_RAG.pdf");
        Path path = Paths.get(fileUrl.toURI());

        DocumentParser documentParser = new ApacheTikaDocumentParser();
        Document document = FileSystemDocumentLoader.loadDocument(path, documentParser);

        DocumentSplitter splitter = DocumentSplitters.recursive(300, 50);
        List<TextSegment> segments = splitter.split(document);

        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();

        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        embeddingStore.addAll(embeddings, segments);

        ContentRetriever retriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(3)
                .minScore(0.5)
                .build();

        QueryRouter queryRouter = new QueryRouter() {
            @Override
            public Collection<ContentRetriever> route(Query query) {
                PromptTemplate template = PromptTemplate.from(
                        "Tâche: dire si la requête suivante concerne l'intelligence artificielle.\n" +
                                "Requête: {{requete}}\n" +
                                "Réponds uniquement par un seul mot en minuscules: oui, non ou peut-être. N'ajoute rien d'autre."
                );
                Prompt prompt = template.apply(Map.of("requete", query.text()));
                String reponse = model.chat(prompt.text());
                return reponse.toLowerCase().contains("non")
                        ? Collections.emptyList()
                        : Collections.singletonList(retriever);
            }
        };

        RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                .queryRouter(queryRouter)
                .build();

        ChatMemory memory = MessageWindowChatMemory.withMaxMessages(10);

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .retrievalAugmentor(retrievalAugmentor)
                .chatMemory(memory)
                .build();

        conversationAvec(assistant);
    }

    private static void conversationAvec(Assistant assistant) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("──────────────────────────────────────────────────");
            System.out.println("Assistant RAG prêt. Saisissez votre question.");
            System.out.println("Tapez « fin » pour quitter.");
            System.out.println("──────────────────────────────────────────────────");
            while (true) {
                System.out.print("Vous > ");
                String question = scanner.nextLine();
                if (!question.isBlank()) {
                    if ("fin".equalsIgnoreCase(question)) {
                        System.out.println("Au revoir.");
                        return;
                    }
                    String reponse = assistant.chat(question);
                    System.out.println("Assistant > " + reponse);
                    System.out.println("──────────────────────────────────────────────────");
                } else {
                    System.out.println("Veuillez entrer une question ou « fin » pour quitter.");
                }
            }
        }
    }
}
