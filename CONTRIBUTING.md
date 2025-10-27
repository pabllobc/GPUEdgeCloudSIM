
# 🤝 Guia de Contribuição - GpuEdgeCloudSim

Obrigado por considerar contribuir para o GpuEdgeCloudSim! Este documento fornece diretrizes para contribuições.

## 📋 Índice

- [Como Contribuir](#como-contribuir)
- [Reportando Bugs](#reportando-bugs)
- [Sugerindo Melhorias](#sugerindo-melhorias)
- [Processo de Pull Request](#processo-de-pull-request)
- [Padrões de Código](#padrões-de-código)
- [Testes](#testes)
- [Documentação](#documentação)

---

## Como Contribuir

Existem várias maneiras de contribuir para o projeto:

1. **Reportar bugs** - Encontrou um problema? Nos avise!
2. **Sugerir melhorias** - Tem ideias para novos recursos?
3. **Corrigir bugs** - Submeta um PR com correções
4. **Implementar features** - Adicione novas funcionalidades
5. **Melhorar documentação** - Ajude a tornar os docs mais claros
6. **Criar tutoriais** - Escreva guias para ajudar outros usuários
7. **Adicionar testes** - Aumente a cobertura de testes

---

## Reportando Bugs

Antes de reportar um bug:

1. ✅ Verifique se já não foi reportado nas [Issues](https://github.com/pabllobc/GPUEdgeCloudSIM/issues)
2. ✅ Certifique-se de estar usando a versão mais recente
3. ✅ Tente reproduzir o bug de forma consistente

### Template de Bug Report

```markdown
**Descrição do Bug**
Uma descrição clara e concisa do bug.

**Como Reproduzir**
Passos para reproduzir o comportamento:
1. Execute comando '...'
2. Com configuração '...'
3. Observe erro '...'

**Comportamento Esperado**
O que você esperava que acontecesse.

**Comportamento Atual**
O que realmente aconteceu.

**Ambiente**
- OS: [e.g. Ubuntu 24.04]
- Java Version: [e.g. OpenJDK 21]
- GpuEdgeCloudSim Version: [e.g. v1.0]

**Logs**
Cole logs relevantes aqui.

**Screenshots**
Se aplicável, adicione screenshots.
```

---

## Sugerindo Melhorias

Sugestões de melhorias são sempre bem-vindas! Para sugerir:

1. Abra uma [Issue](https://github.com/pabllobc/GPUEdgeCloudSIM/issues/new)
2. Use o label `enhancement`
3. Descreva claramente a melhoria proposta
4. Explique o benefício e casos de uso

### Áreas de Melhoria

- **Novas políticas de escalonamento**
- **Novos modelos de GPU**
- **Melhorias de performance**
- **Novos cenários científicos**
- **Visualizações aprimoradas**
- **Integração com outras ferramentas**

---

## Processo de Pull Request

### Antes de Submeter

1. **Fork** o repositório
2. **Clone** seu fork localmente
3. **Crie uma branch** para sua feature:
   ```bash
   git checkout -b feature/MinhaNovaFeature
   ```

### Durante o Desenvolvimento

1. **Siga os padrões de código** (veja abaixo)
2. **Escreva testes** para novas funcionalidades
3. **Atualize a documentação** quando necessário
4. **Commit** com mensagens descritivas:
   ```bash
   git commit -m "Adiciona política de escalonamento baseada em prioridade"
   ```

### Submetendo o PR

1. **Push** sua branch:
   ```bash
   git push origin feature/MinhaNovaFeature
   ```

2. Abra um **Pull Request** no GitHub

3. **Descreva suas mudanças:**
   - O que foi alterado?
   - Por que foi alterado?
   - Como testar as mudanças?

4. **Aguarde review** - responda a comentários e faça ajustes se necessário

### Template de Pull Request

```markdown
## Descrição

Breve descrição do que este PR faz.

## Tipo de Mudança

- [ ] Bug fix (non-breaking change)
- [ ] Nova feature (non-breaking change)
- [ ] Breaking change (fix ou feature que muda comportamento existente)
- [ ] Documentação

## Como Foi Testado?

Descreva os testes realizados.

## Checklist

- [ ] Meu código segue os padrões do projeto
- [ ] Realizei self-review do código
- [ ] Comentei áreas complexas do código
- [ ] Atualizei a documentação
- [ ] Minhas mudanças não geram novos warnings
- [ ] Adicionei testes que provam que o fix/feature funciona
- [ ] Testes unitários passam localmente
- [ ] Testes de integração passam (se aplicável)
```

---

## Padrões de Código

### Convenções Java

1. **Nomenclatura:**
   - Classes: `PascalCase` (ex: `GpuEdgeHost`)
   - Métodos: `camelCase` (ex: `allocateGpuResources`)
   - Constantes: `UPPER_SNAKE_CASE` (ex: `MAX_GPU_COUNT`)
   - Variáveis: `camelCase` (ex: `gpuUtilization`)

2. **Estrutura:**
   - Indentação: 4 espaços (não tabs)
   - Máximo 120 caracteres por linha
   - Chaves no mesmo estilo do EdgeCloudSim original

3. **Documentação:**
   - Todas as classes públicas devem ter JavaDoc
   - Métodos públicos devem ter JavaDoc
   - Comentários inline para lógica complexa

### Exemplo de Código

```java
/**
 * Representa uma GPU em um EdgeHost.
 * 
 * @author Pabllo Borges Cardoso
 * @since GpuEdgeCloudSim v1.0
 */
public class Gpu {
    
    /** Número de compute units disponíveis */
    private int computeUnits;
    
    /** Memória total em MB */
    private int memory;
    
    /**
     * Aloca recursos da GPU para uma tarefa.
     * 
     * @param task A tarefa que requer recursos GPU
     * @return true se a alocação foi bem-sucedida, false caso contrário
     */
    public boolean allocateResources(GpuTask task) {
        // Lógica de alocação
        return true;
    }
}
```

---

## Testes

### Estrutura de Testes

```
test/
└── edu/boun/edgecloudsim/
    ├── edge_server/
    │   ├── GpuTest.java
    │   ├── GpuEdgeHostTest.java
    │   └── GpuProvisionerTest.java
    └── applications/gpusim/
        ├── GpuScenarioFactoryIntegrationTest.java
        └── GpuEndToEndIntegrationTest.java
```

### Executando Testes

```bash
# Todos os testes
mvn test

# Teste específico
mvn test -Dtest=GpuTest

# Com cobertura
mvn test jacoco:report
```

### Escrevendo Novos Testes

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GpuTest {
    
    @Test
    void testGpuAllocation() {
        Gpu gpu = new Gpu(1024, 8192);
        GpuTask task = new GpuTask(512, 4096);
        
        assertTrue(gpu.allocateResources(task));
        assertEquals(512, gpu.getAvailableComputeUnits());
    }
}
```

---

## Documentação

### Atualizando Documentação

Quando adicionar ou modificar funcionalidades:

1. ✅ Atualize o `README.md` se necessário
2. ✅ Adicione/atualize JavaDoc nas classes
3. ✅ Crie/atualize guias em `docs/`
4. ✅ Atualize exemplos em `scenarios/`

### Estrutura de Documentação

```
docs/
├── fases/              # Documentação das fases de desenvolvimento
├── analises/           # Análises científicas
├── guides/             # Guias de uso
└── api/                # Documentação de API (JavaDoc)
```

---

## Código de Conduta

### Nossos Valores

- 🤝 **Respeito:** Trate todos com respeito e cortesia
- 🌍 **Inclusão:** Seja acolhedor e inclusivo
- 💡 **Colaboração:** Trabalhe em conjunto construtivamente
- 📚 **Aprendizado:** Compartilhe conhecimento e aprenda com outros

### Comportamentos Esperados

- ✅ Usar linguagem acolhedora e inclusiva
- ✅ Respeitar pontos de vista diferentes
- ✅ Aceitar críticas construtivas graciosamente
- ✅ Focar no que é melhor para a comunidade

### Comportamentos Inaceitáveis

- ❌ Linguagem ou imagens sexualizadas
- ❌ Ataques pessoais ou políticos
- ❌ Assédio público ou privado
- ❌ Publicar informações privadas de outros

---

## Perguntas?

Tem dúvidas sobre como contribuir?

- 💬 Abra uma [Discussion](https://github.com/pabllobc/GPUEdgeCloudSIM/discussions)
- 📧 Entre em contato via Issues
- 📚 Consulte a [documentação](docs/)

---

## Agradecimentos

Obrigado por contribuir para o GpuEdgeCloudSim! Sua ajuda é fundamental para o crescimento do projeto. 🙏

---

**Última atualização:** Outubro 2025
