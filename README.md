# Connecta Gestor - Sistema de Gestão Municipal

Sistema completo de gestão municipal com autenticação JWT, controle de protocolos, gerenciamento de serviços e comunicados aos cidadãos.

## 🚀 Tecnologias

- **Java 20**
- **Spring Boot 3.2.1**
- **PostgreSQL** (banco de dados)
- **Spring Security** + **JWT** (autenticação e autorização)
- **Spring Mail** (envio de emails)
- **Lombok** (redução de boilerplate)
- **Maven** (gerenciamento de dependências)

## 📋 Pré-requisitos

- Java 20 ou superior
- PostgreSQL 12 ou superior
- Maven 3.6 ou superior
- Conta Gmail para envio de emails (ou outro provedor SMTP)

## ⚙️ Configuração

### 1. Banco de Dados PostgreSQL

1. Instale e inicie o PostgreSQL
2. Crie o banco de dados:
```sql
CREATE DATABASE connecta_gestor;
```

3. Configure as credenciais em `application.properties` se necessário:
```properties
spring.datasource.username=postgres
spring.datasource.password=094695@Lpg
```

### 2. Configuração de Email

Para envio de emails, configure uma conta Gmail no arquivo `application.properties`:

1. Acesse sua conta Google
2. Ative a verificação em duas etapas
3. Gere uma "Senha de app" em: https://myaccount.google.com/apppasswords
4. Configure no `application.properties`:

```properties
spring.mail.username=seu-email@gmail.com
spring.mail.password=sua-senha-de-app
```

**Nota:** A senha de app é diferente da senha da sua conta Google.

## 🏃 Como Executar

### 1. Compilar o projeto

```bash
mvn clean install
```

### 2. Executar a aplicação

```bash
mvn spring-boot:run
```

A aplicação estará disponível em: `http://localhost:8080`

### 3. Dados Iniciais

Ao iniciar pela primeira vez, serão criados automaticamente:

#### Usuário Root
- **Email:** lucaspenna@wimagroup.com.br
- **Senha:** admin0946
- **Role:** SUPER_ADMIN

**⚠️ IMPORTANTE:** Altere a senha após o primeiro login!

#### Categorias e Serviços Base
- 8 categorias pré-cadastradas (Meio Ambiente, Saneamento, Infraestrutura, etc.)
- 12 serviços base com campos configuráveis

## 🔐 Roles (Perfis de Acesso)

O sistema possui 5 roles pré-definidas:

1. **ROLE_SUPER_ADMIN** - Super Administrador (acesso total)
2. **ROLE_GESTOR** - Gestor (gerenciamento completo)
3. **ROLE_ATENDENTE** - Atendente (gestão de protocolos)
4. **ROLE_FINANCEIRO** - Financeiro
5. **ROLE_VISUALIZADOR** - Visualizador (apenas leitura)

## 📦 Módulos do Sistema

### 1. Autenticação e Usuários
- Login com JWT
- Recuperação de senha por email
- Gerenciamento de usuários
- Controle de acesso por roles

### 2. Categorias e Serviços
- CRUD completo de categorias
- CRUD completo de serviços
- Campos configuráveis por serviço
- Serviços com prazos e descrições

### 3. Protocolos
- Criação de protocolos de atendimento
- Geração automática de número (#YYYY0001)
- Atribuição para atendentes
- Controle de status e prioridades
- Histórico completo de ações
- Sistema de comentários (internos e públicos)
- Cálculo automático de prazos
- Detecção de protocolos atrasados
- Estatísticas e dashboard

### 4. Comunicados
- Envio de comunicados aos cidadãos
- Múltiplos canais (Email, SMS, App)
- Agendamento de envios
- Rascunhos e edição
- Controle de destinatários
- Rastreamento de envios e erros
- Estatísticas de comunicação

## 📡 Endpoints da API

### Autenticação (Públicos)

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "usuario@email.com",
  "senha": "senha123"
}
```

#### Recuperar Senha
```http
POST /api/auth/recovery-password
Content-Type: application/json

{
  "email": "usuario@email.com"
}
```

### Categorias

```http
GET    /api/categorias              # Listar todas
GET    /api/categorias/ativas       # Listar apenas ativas (público)
GET    /api/categorias/{id}         # Buscar por ID
POST   /api/categorias              # Criar (SUPER_ADMIN, GESTOR)
PUT    /api/categorias/{id}         # Atualizar (SUPER_ADMIN, GESTOR)
DELETE /api/categorias/{id}         # Deletar (SUPER_ADMIN, GESTOR)
PATCH  /api/categorias/{id}/toggle-status  # Ativar/Desativar
```

### Serviços

```http
GET    /api/servicos                     # Listar todos
GET    /api/servicos/ativos              # Listar ativos (público)
GET    /api/servicos/categoria/{id}      # Por categoria (público)
GET    /api/servicos/{id}                # Buscar por ID
POST   /api/servicos                     # Criar (SUPER_ADMIN, GESTOR)
PUT    /api/servicos/{id}                # Atualizar (SUPER_ADMIN, GESTOR)
DELETE /api/servicos/{id}                # Deletar (SUPER_ADMIN, GESTOR)
PATCH  /api/servicos/{id}/toggle-status  # Ativar/Desativar
```

### Protocolos

```http
GET    /api/protocolos                      # Listar todos
GET    /api/protocolos/status/{status}      # Por status
GET    /api/protocolos/atendente/{id}       # Por atendente
GET    /api/protocolos/atrasados            # Atrasados
GET    /api/protocolos/{id}                 # Buscar por ID
GET    /api/protocolos/numero/{numero}      # Buscar por número
POST   /api/protocolos                      # Criar (público)
PATCH  /api/protocolos/{id}/atribuir        # Atribuir atendente
PATCH  /api/protocolos/{id}/status          # Alterar status
PATCH  /api/protocolos/{id}/prioridade      # Alterar prioridade
POST   /api/protocolos/{id}/comentarios     # Adicionar comentário
PATCH  /api/protocolos/{id}/finalizar       # Finalizar
GET    /api/protocolos/estatisticas         # Estatísticas
```

### Comunicados

```http
GET    /api/comunicados                   # Listar todos
GET    /api/comunicados/status/{status}   # Por status
GET    /api/comunicados/meus              # Meus comunicados
GET    /api/comunicados/{id}              # Buscar por ID
GET    /api/comunicados/{id}/destinatarios # Ver destinatários
POST   /api/comunicados                   # Criar
PUT    /api/comunicados/{id}              # Atualizar (rascunhos)
POST   /api/comunicados/{id}/enviar       # Enviar
PATCH  /api/comunicados/{id}/cancelar     # Cancelar
DELETE /api/comunicados/{id}              # Deletar (rascunhos)
GET    /api/comunicados/estatisticas      # Estatísticas
```

## 🔒 Como Usar o JWT

1. Faça login no endpoint `/api/auth/login`
2. Copie o token recebido
3. Adicione o header `Authorization` em todas as requisições protegidas:
   ```
   Authorization: Bearer {seu-token-jwt}
   ```

**Exemplo com cURL:**
```bash
curl -X GET http://localhost:8080/api/protocolos \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."
```

## 📧 Funcionalidades de Email

O sistema envia emails automaticamente nas seguintes situações:

1. **Bem-vindo** - Quando um novo usuário é criado
2. **Recuperação de senha** - Quando solicitada recuperação
3. **Confirmação de alteração** - Quando a senha é alterada
4. **Comunicados** - Envio de comunicados para cidadãos

## 🛡️ Segurança

- **Senhas criptografadas** com BCrypt (strength 10)
- **Tokens JWT** com expiração de 24 horas
- **Token de recuperação** expira em 1 hora
- **CORS configurado** para aceitar localhost:4200
- **Validação de dados** com Bean Validation
- **Controle de acesso** com @PreAuthorize

## 📦 Estrutura do Projeto

```
src/main/java/com/connecta/gestor/
├── config/              # Configurações (Security, DataInitializer)
├── controller/          # Controllers REST
│   ├── AuthController
│   ├── UserController
│   ├── CategoriaController
│   ├── ServicoController
│   ├── ProtocoloController
│   └── ComunicadoController
├── dto/                 # Data Transfer Objects (37 DTOs)
├── exception/           # Exceções customizadas e handlers
├── model/               # Entidades JPA
│   ├── enums/          # Enumerações (8 enums)
│   ├── User, Role
│   ├── Categoria, Servico, ServicoCampo
│   ├── Protocolo, ProtocoloDado, ProtocoloHistorico, ProtocoloComentario
│   └── Comunicado, ComunicadoDestinatario
├── repository/          # Repositórios Spring Data (11 repositories)
├── security/            # Classes de segurança JWT
└── service/             # Lógica de negócio (6 services)
```

## 🐛 Troubleshooting

### Erro de conexão com PostgreSQL
- Verifique se o PostgreSQL está rodando
- Confirme as credenciais no `application.properties`
- Certifique-se de que o banco `connecta_gestor` foi criado

### Erro ao enviar emails
- Verifique as configurações SMTP no `application.properties`
- Use uma senha de app do Gmail (não a senha normal)
- Verifique se a verificação em duas etapas está ativa

### Token JWT inválido
- Tokens expiram após 24 horas
- Faça login novamente para obter um novo token

## 📝 Logs

A aplicação gera logs detalhados de todas as operações:
- Login e autenticação
- Criação de usuários e protocolos
- Envio de emails e comunicados
- Alterações de status
- Erros e exceções

## 🧪 Testando a API

Você pode usar ferramentas como:
- **Postman** - https://www.postman.com/
- **Insomnia** - https://insomnia.rest/
- **Thunder Client** (extensão VS Code)
- **cURL** (linha de comando)

## 🎯 Status do Projeto

**✅ Módulos Implementados:**
- ✅ Autenticação e Autorização (JWT + Roles)
- ✅ Gerenciamento de Usuários
- ✅ Categorias e Serviços
- ✅ Protocolos de Atendimento
- ✅ Comunicados aos Cidadãos
- ✅ Sistema de Email
- ✅ Testes Unitários (parcial)

**📋 Próximas Implementações:**
- Portal do Cidadão (integração com protocolos)
- Notificações Push
- Envio de SMS
- Scheduler para comunicados agendados
- Dashboard completo com gráficos

## 📄 Licença

Este projeto é proprietário da Connecta/Wima Group.

## 👥 Suporte

Para suporte, entre em contato com a equipe de desenvolvimento.
