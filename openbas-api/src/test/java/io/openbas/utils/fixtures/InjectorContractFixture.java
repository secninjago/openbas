package io.openbas.utils.fixtures;

import static io.openbas.database.model.InjectorContract.CONTRACT_CONTENT_FIELDS;
import static io.openbas.database.model.InjectorContract.CONTRACT_ELEMENT_CONTENT_KEY_TARGETED_PROPERTY;
import static io.openbas.injector_contract.fields.ContractSelect.selectFieldWithDefault;
import static io.openbas.utils.fixtures.InjectorFixture.createDefaultPayloadInjector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.openbas.database.model.Endpoint;
import io.openbas.database.model.Injector;
import io.openbas.database.model.InjectorContract;
import io.openbas.database.model.Payload;
import io.openbas.injector_contract.ContractCardinality;
import io.openbas.injector_contract.ContractTargetedProperty;
import io.openbas.injector_contract.fields.ContractElement;
import io.openbas.injector_contract.fields.ContractSelect;
import io.openbas.injector_contract.fields.ContractTargetedAsset;
import java.time.Instant;
import java.util.*;
import lombok.SneakyThrows;

public class InjectorContractFixture {

  private static ObjectNode createDefaultContent(ObjectMapper objectMapper) {
    ObjectNode node = objectMapper.createObjectNode();
    node.set(CONTRACT_CONTENT_FIELDS, objectMapper.valueToTree(new ArrayList<>()));
    return node;
  }

  public static InjectorContract createPayloadInjectorContractWithFieldsContent(
      Injector injector, Payload payloadCommand, List<ContractElement> customFieldsContent)
      throws JsonProcessingException {
    InjectorContract injectorContract = new InjectorContract();
    injectorContract.setInjector(injector);
    injectorContract.setPayload(payloadCommand);
    injectorContract.setId(UUID.randomUUID().toString());

    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode content = createDefaultContent(objectMapper);
    content.set(CONTRACT_CONTENT_FIELDS, objectMapper.valueToTree(customFieldsContent));

    injectorContract.setContent(objectMapper.writeValueAsString(content));
    injectorContract.setConvertedContent(content);

    return injectorContract;
  }

  @SneakyThrows
  public static InjectorContract createDefaultInjectorContract() {
    InjectorContract injectorContract = new InjectorContract();
    injectorContract.setInjector(createDefaultPayloadInjector());
    injectorContract.setId(UUID.randomUUID().toString());

    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode content = createDefaultContent(objectMapper);
    injectorContract.setContent(objectMapper.writeValueAsString(content));
    injectorContract.setConvertedContent(content);
    return injectorContract;
  }

  public static InjectorContract createInjectorContractWithPlatforms(
      Endpoint.PLATFORM_TYPE[] platforms) {
    InjectorContract injectorContract = createDefaultInjectorContract();
    injectorContract.setPlatforms(platforms);
    return injectorContract;
  }

  public static InjectorContract createPayloadInjectorContract(
      Injector injector, Payload payloadCommand) throws JsonProcessingException {
    return createPayloadInjectorContractWithFieldsContent(injector, payloadCommand, List.of());
  }

  public static InjectorContract createPayloadInjectorContractWithObfuscator(
      Injector injector, Payload payloadCommand) throws JsonProcessingException {
    ContractSelect obfuscatorSelect =
        new ContractSelect("obfuscator", "Obfuscators", ContractCardinality.One);
    obfuscatorSelect.setChoices(Map.of("plain-text", "plain-text", "base64", "base64"));

    return createPayloadInjectorContractWithFieldsContent(
        injector, payloadCommand, List.of(obfuscatorSelect));
  }

  public static InjectorContract createInjectorContract(Map<String, String> labels, String content)
      throws JsonProcessingException {
    InjectorContract injectorContract = new InjectorContract();
    injectorContract.setId(UUID.randomUUID().toString());
    injectorContract.setLabels(labels);
    injectorContract.setContent(content);
    injectorContract.setConvertedContent(new ObjectMapper().readValue(content, ObjectNode.class));
    injectorContract.setAtomicTesting(true);
    injectorContract.setCreatedAt(Instant.now());
    injectorContract.setUpdatedAt(Instant.now());
    return injectorContract;
  }

  public static InjectorContract createInjectorContract(Map<String, String> labels)
      throws JsonProcessingException {
    String content = "{\"fields\": []}";
    return createInjectorContract(labels, content);
  }

  // Method to convert ContractElement to JsonNode
  public JsonNode toJsonNode() {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.valueToTree(this); // Converts this object to a JsonNode
  }

  public static void addTargetedAssetFields(
      InjectorContract injectorContract,
      String key,
      ContractTargetedProperty defaultTargetedProperty) {
    ContractElement targetedAssetField = new ContractTargetedAsset(key, "label-" + key);
    ContractElement targetPropertySelector =
        selectFieldWithDefault(
            CONTRACT_ELEMENT_CONTENT_KEY_TARGETED_PROPERTY + "-" + key,
            "Targeted Property",
            new HashMap<>(),
            defaultTargetedProperty.name());
    targetPropertySelector.setLinkedFields(List.of(targetedAssetField));

    JsonNode injectorContractFieldsNode =
        injectorContract.getConvertedContent().get(CONTRACT_CONTENT_FIELDS);

    if (!(injectorContractFieldsNode instanceof ArrayNode)) {
      throw new IllegalArgumentException("The fields node is not an ArrayNode");
    }

    ArrayNode arrayNode = (ArrayNode) injectorContractFieldsNode;
    ObjectMapper objectMapper = new ObjectMapper();

    arrayNode.add(objectMapper.valueToTree(targetedAssetField));
    arrayNode.add(objectMapper.valueToTree(targetPropertySelector));
    injectorContract.getConvertedContent().set(CONTRACT_CONTENT_FIELDS, arrayNode);
  }
}
