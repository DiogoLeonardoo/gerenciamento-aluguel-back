# inHouse - Sistema de Gestão de Aluguel de Imóveis

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-6DB33F?style=flat&logo=spring)
![Java](https://img.shields.io/badge/Java-17-007396?style=flat&logo=java)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Latest-336791?style=flat&logo=postgresql)
![JWT](https://img.shields.io/badge/JWT-Authentication-000000?style=flat&logo=json-web-tokens)
![Swagger](https://img.shields.io/badge/Swagger-API%20Docs-85EA2D?style=flat&logo=swagger)

## 📖 Descrição

O inHouse é uma aplicação de backend desenvolvida para gerenciar o processo de aluguel de casas e imóveis. A plataforma permite que proprietários cadastrem suas propriedades, hóspedes reservem os imóveis disponíveis e administradores supervisionem todo o sistema.

## 🚀 Funcionalidades

- **Gestão de Imóveis**
  - Cadastro completo de casas e propriedades
  - Upload e gerenciamento de fotos dos imóveis
  - Controle de inventário de itens da casa

- **Sistema de Reservas**
  - Reservas de propriedades com checkin/checkout
  - Acompanhamento de status de reservas
  - Cálculo automático de valores

- **Gestão de Usuários**
  - Níveis de acesso: Administradores, Proprietários e Hóspedes
  - Autenticação segura com JWT
  - Controle de permissões baseado em papéis (RBAC)

- **Contratos**
  - Gestão de contratos para reservas
  - Templates de contratos personalizáveis

- **API Restful**
  - Endpoints documentados com OpenAPI/Swagger
  - Segurança com Spring Security
  - Tratamento global de exceções

## 🛠️ Tecnologias Utilizadas

### Backend
- **Spring Boot 3.5.4** - Framework para desenvolvimento da aplicação
- **Java 17** - Linguagem de programação
- **Spring Data JPA** - Persistência de dados
- **Spring Security** - Segurança e autenticação
- **JWT** - Autenticação baseada em tokens
- **PostgreSQL** - Banco de dados relacional
- **Lombok** - Redução de código boilerplate
- **Spring Validation** - Validação de dados
- **SpringDoc OpenAPI** - Documentação da API

## 🗄️ Estrutura do Projeto

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── inhouse/
│   │           └── project/
│   │               ├── config/            # Configurações da aplicação
│   │               ├── domain/            # Entidades de domínio
│   │               ├── exceptions/        # Tratamento de exceções
│   │               ├── repository/        # Repositórios JPA
│   │               ├── resource/          # Endpoints da API
│   │               ├── security/          # Configuração de segurança e JWT
│   │               └── service/           # Lógica de negócio
│   └── resources/
│       ├── application.properties         # Propriedades da aplicação
│       └── static/                        # Recursos estáticos
└── test/
    └── java/...                           # Testes automatizados
```

## 📝 Entidades Principais

- **Casa**: Propriedades disponíveis para aluguel
- **Proprietario**: Proprietários de imóveis
- **Hospede**: Usuários que alugam os imóveis
- **Reserva**: Registros de reservas de imóveis
- **InventarioCasa**: Controle de itens e condições dos imóveis
- **Usuarios**: Gerenciamento de usuários do sistema
- **FotoCasa**: Armazenamento de fotos dos imóveis
- **Contrato/TemplateContrato**: Gestão de documentos legais

## 🛡️ Segurança

O sistema utiliza Spring Security com autenticação JWT (JSON Web Token) para proteger os endpoints da API. Cada tipo de usuário (ADMIN, PROPRIETARIO, HOSPEDE) tem permissões específicas para acessar determinados recursos.

## 🌐 API Endpoints

A documentação completa da API está disponível via Swagger UI:

```
http://localhost:8080/swagger-ui.html
```

### Principais endpoints:

#### Autenticação
- `POST /auth/register` - Registro de novos usuários
- `POST /auth/login` - Login e obtenção de token JWT

#### Casas
- `GET /api/casas` - Lista de imóveis disponíveis
- `POST /api/casas` - Cadastro de novo imóvel (requer autenticação de proprietário)
- `GET /api/casas/{id}` - Detalhes de um imóvel específico
- `PUT /api/casas/{id}` - Atualização de imóvel (proprietário ou admin)
- `DELETE /api/casas/{id}` - Remoção de imóvel (proprietário ou admin)

#### Reservas
- `GET /api/reservas` - Lista de reservas
- `POST /api/reservas` - Criação de nova reserva
- `PUT /api/reservas/{id}/status` - Atualização de status de reserva

## 🚀 Configuração e Execução

### Pré-requisitos
- Java 17+
- Maven
- PostgreSQL

### Configuração do Banco de Dados
1. Crie um banco de dados PostgreSQL chamado `sistema_aluguel`
2. Configure o usuário e senha no arquivo `application.properties` (veja o exemplo em `application.properties.example`)

### Ambiente de Produção
O backend está hospedado em: **https://inhouse-back-production.up.railway.app/**

### Configuração CORS
O sistema está configurado para aceitar requisições dos seguintes domínios:
- `http://localhost:3000` (desenvolvimento local)
- `http://127.0.0.1:3000` (desenvolvimento local)
- `https://in-house-front-git-dev-diogo-leonardos-projects.vercel.app` (frontend em produção)

**Nota:** A configuração CORS é gerenciada diretamente pelo Spring Security (`SecurityConfig.java`) para garantir que as requisições sejam processadas corretamente antes da autenticação. Permite métodos GET, POST, PUT, PATCH, DELETE e OPTIONS, com suporte a credenciais (cookies/tokens).

### Execução do Projeto
```bash
# Compilar e empacotar o projeto
mvn clean package

# Executar a aplicação
java -jar target/project-0.0.1-SNAPSHOT.jar
```

Alternativamente, você pode executar diretamente via Maven:
```bash
mvn spring-boot:run
```

## 🧪 Testes

Para executar os testes:

```bash
mvn test
```

## 🔄 Branchs

- `main` - Código de produção estável
- `fix/hospede-proprietario` - Branch atual para correções relacionadas a hóspedes e proprietários

## 📜 Licença

Este projeto está sob a licença MIT.

## 👥 Contribuidores

- Equipe inHouse

---

Desenvolvido como parte do projeto inHouse para gerenciamento de imóveis e reservas.
