# 📋 API de Serviços - Documentação Completa

## 🎯 Base URL
```
http://localhost:8080/api/servicos
```

---

## 🔐 Autenticação

**Rotas Públicas** (sem token):
- `GET /api/servicos/ativos`
- `GET /api/servicos/categoria/{categoriaId}`

**Rotas Protegidas** (requerem JWT):
- Todas as demais rotas

**Header de autenticação:**
```
Authorization: Bearer {seu-access-token}
```

---

## 📡 Endpoints

### 1. Listar com Filtros e Paginação 🔍

**Endpoint:** `GET /api/servicos`

**Autenticação:** ✅ Requer (SUPER_ADMIN, GESTOR, ATENDENTE)

**Query Parameters:**
| Parâmetro | Tipo | Obrigatório | Default | Descrição |
|-----------|------|-------------|---------|-----------|
| `busca` | String | Não | - | Busca por nome (LIKE) |
| `categorias` | Long[] | Não | - | IDs das categorias (multiselect) |
| `status` | Boolean[] | Não | - | Status ativo/inativo (multiselect) |
| `page` | Integer | Não | 0 | Número da página |
| `size` | Integer | Não | 10 | Tamanho da página |
| `sort` | String | Não | titulo | Campo para ordenação |
| `direction` | String | Não | asc | Direção (asc/desc) |

**Exemplo de Requisição:**
```http
GET /api/servicos?busca=poda&categorias=1,2&status=true&page=0&size=10&sort=titulo&direction=asc
Authorization: Bearer eyJhbGc...
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "categoriaId": 1,
      "categoriaNome": "Meio Ambiente",
      "categoriaIcone": "tree",
      "categoriaCor": "#4CAF50",
      "titulo": "Poda de Árvore",
      "descricao": "Solicitação de poda de árvore em via pública",
      "prazoAtendimentoDias": 15,
      "ativo": true,
      "campos": [
        {
          "id": 1,
          "campoTipo": "LOCALIZACAO",
          "campoLabel": "Localização",
          "campoDescricao": "Endereço completo, CEP, bairro, ponto de referência",
          "obrigatorio": true,
          "ordem": 1,
          "instrucoes": "Informe o endereço exato da árvore"
        }
      ],
      "createdAt": "2025-12-10T10:00:00",
      "updatedAt": "2025-12-10T10:00:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalElements": 12,
  "totalPages": 2,
  "last": false,
  "first": true,
  "size": 10,
  "number": 0,
  "numberOfElements": 10,
  "empty": false
}
```

---

### 2. Listar Todos (sem paginação) 📋

**Endpoint:** `GET /api/servicos/todos`

**Autenticação:** ✅ Requer (SUPER_ADMIN, GESTOR)

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "categoriaId": 1,
    "categoriaNome": "Meio Ambiente",
    "categoriaIcone": "tree",
    "categoriaCor": "#4CAF50",
    "titulo": "Poda de Árvore",
    "descricao": "Solicitação de poda de árvore em via pública",
    "prazoAtendimentoDias": 15,
    "ativo": true,
    "campos": [...],
    "createdAt": "2025-12-10T10:00:00",
    "updatedAt": "2025-12-10T10:00:00"
  }
]
```

---

### 3. Listar Ativos (Público) ✅

**Endpoint:** `GET /api/servicos/ativos`

**Autenticação:** ❌ Não requer

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "categoriaId": 1,
    "categoriaNome": "Meio Ambiente",
    "categoriaIcone": "tree",
    "categoriaCor": "#4CAF50",
    "titulo": "Poda de Árvore",
    "descricao": "Solicitação de poda de árvore em via pública",
    "prazoAtendimentoDias": 15,
    "ativo": true,
    "campos": [...],
    "createdAt": "2025-12-10T10:00:00",
    "updatedAt": "2025-12-10T10:00:00"
  }
]
```

---

### 4. Listar por Categoria (Público) 📁

**Endpoint:** `GET /api/servicos/categoria/{categoriaId}`

**Autenticação:** ❌ Não requer

**Path Parameter:**
- `categoriaId` (Long): ID da categoria

**Exemplo:**
```http
GET /api/servicos/categoria/1
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "categoriaId": 1,
    "categoriaNome": "Meio Ambiente",
    "categoriaIcone": "tree",
    "categoriaCor": "#4CAF50",
    "titulo": "Poda de Árvore",
    "descricao": "Solicitação de poda de árvore em via pública",
    "prazoAtendimentoDias": 15,
    "ativo": true,
    "campos": [...],
    "createdAt": "2025-12-10T10:00:00",
    "updatedAt": "2025-12-10T10:00:00"
  }
]
```

**Erros:**
- `404 Not Found`: Categoria não encontrada

---

### 5. Buscar por ID 🔎

**Endpoint:** `GET /api/servicos/{id}`

**Autenticação:** ✅ Requer (SUPER_ADMIN, GESTOR, ATENDENTE)

**Path Parameter:**
- `id` (Long): ID do serviço

**Exemplo:**
```http
GET /api/servicos/1
Authorization: Bearer eyJhbGc...
```

**Response (200 OK):**
```json
{
  "id": 1,
  "categoriaId": 1,
  "categoriaNome": "Meio Ambiente",
  "categoriaIcone": "tree",
  "categoriaCor": "#4CAF50",
  "titulo": "Poda de Árvore",
  "descricao": "Solicitação de poda de árvore em via pública",
  "prazoAtendimentoDias": 15,
  "ativo": true,
  "campos": [
    {
      "id": 1,
      "campoTipo": "LOCALIZACAO",
      "campoLabel": "Localização",
      "campoDescricao": "Endereço completo, CEP, bairro, ponto de referência",
      "obrigatorio": true,
      "ordem": 1,
      "instrucoes": "Informe o endereço exato da árvore"
    }
  ],
  "createdAt": "2025-12-10T10:00:00",
  "updatedAt": "2025-12-10T10:00:00"
}
```

**Erros:**
- `404 Not Found`: Serviço não encontrado

---

### 6. Criar Serviço ➕

**Endpoint:** `POST /api/servicos`

**Autenticação:** ✅ Requer (SUPER_ADMIN, GESTOR)

**Request Body:**
```json
{
  "categoriaId": 1,
  "titulo": "Novo Serviço",
  "descricao": "Descrição do serviço",
  "prazoAtendimentoDias": 10,
  "campos": [
    {
      "campoTipo": "LOCALIZACAO",
      "obrigatorio": true,
      "ordem": 1,
      "instrucoes": "Instruções específicas"
    },
    {
      "campoTipo": "FOTO",
      "obrigatorio": false,
      "ordem": 2,
      "instrucoes": null
    }
  ]
}
```

**Validações:**
- `categoriaId`: Obrigatório, deve existir
- `titulo`: Obrigatório, máximo 150 caracteres, único
- `descricao`: Opcional
- `prazoAtendimentoDias`: Obrigatório, mínimo 1
- `campos`: Opcional, lista de campos configuráveis

**Response (201 Created):**
```json
{
  "id": 13,
  "categoriaId": 1,
  "categoriaNome": "Meio Ambiente",
  "categoriaIcone": "tree",
  "categoriaCor": "#4CAF50",
  "titulo": "Novo Serviço",
  "descricao": "Descrição do serviço",
  "prazoAtendimentoDias": 10,
  "ativo": true,
  "campos": [
    {
      "id": 25,
      "campoTipo": "LOCALIZACAO",
      "campoLabel": "Localização",
      "campoDescricao": "Endereço completo, CEP, bairro, ponto de referência",
      "obrigatorio": true,
      "ordem": 1,
      "instrucoes": "Instruções específicas"
    }
  ],
  "createdAt": "2025-12-10T10:30:00",
  "updatedAt": "2025-12-10T10:30:00"
}
```

**Erros:**
- `400 Bad Request`: Validação falhou
- `404 Not Found`: Categoria não encontrada
- `409 Conflict`: Título já existe

---

### 7. Atualizar Serviço ✏️

**Endpoint:** `PUT /api/servicos/{id}`

**Autenticação:** ✅ Requer (SUPER_ADMIN, GESTOR)

**Path Parameter:**
- `id` (Long): ID do serviço

**Request Body:**
```json
{
  "categoriaId": 2,
  "titulo": "Título Atualizado",
  "descricao": "Nova descrição",
  "prazoAtendimentoDias": 20,
  "ativo": true,
  "campos": [
    {
      "campoTipo": "LOCALIZACAO",
      "obrigatorio": true,
      "ordem": 1,
      "instrucoes": "Novas instruções"
    }
  ]
}
```

**Observações:**
- Todos os campos são opcionais
- Se `campos` for enviado, substitui completamente os campos existentes
- Se `campos` não for enviado, mantém os campos atuais

**Response (200 OK):**
```json
{
  "id": 1,
  "categoriaId": 2,
  "categoriaNome": "Saneamento",
  "categoriaIcone": "water_drop",
  "categoriaCor": "#2196F3",
  "titulo": "Título Atualizado",
  "descricao": "Nova descrição",
  "prazoAtendimentoDias": 20,
  "ativo": true,
  "campos": [...],
  "createdAt": "2025-12-10T10:00:00",
  "updatedAt": "2025-12-10T10:35:00"
}
```

**Erros:**
- `400 Bad Request`: Validação falhou
- `404 Not Found`: Serviço ou categoria não encontrada
- `409 Conflict`: Título já existe

---

### 8. Deletar Serviço 🗑️

**Endpoint:** `DELETE /api/servicos/{id}`

**Autenticação:** ✅ Requer (SUPER_ADMIN, GESTOR)

**Path Parameter:**
- `id` (Long): ID do serviço

**Exemplo:**
```http
DELETE /api/servicos/1
Authorization: Bearer eyJhbGc...
```

**Response (204 No Content)**

**Erros:**
- `404 Not Found`: Serviço não encontrado

**⚠️ ATENÇÃO:** Esta operação é **permanente** e deleta também todos os campos associados (cascade).

---

### 9. Ativar/Desativar Serviço 🔄

**Endpoint:** `PATCH /api/servicos/{id}/toggle-status`

**Autenticação:** ✅ Requer (SUPER_ADMIN, GESTOR)

**Path Parameter:**
- `id` (Long): ID do serviço

**Exemplo:**
```http
PATCH /api/servicos/1/toggle-status
Authorization: Bearer eyJhbGc...
```

**Response (200 OK):**
```json
{
  "id": 1,
  "categoriaId": 1,
  "categoriaNome": "Meio Ambiente",
  "categoriaIcone": "tree",
  "categoriaCor": "#4CAF50",
  "titulo": "Poda de Árvore",
  "descricao": "Solicitação de poda de árvore em via pública",
  "prazoAtendimentoDias": 15,
  "ativo": false,
  "campos": [...],
  "createdAt": "2025-12-10T10:00:00",
  "updatedAt": "2025-12-10T10:40:00"
}
```

**Erros:**
- `404 Not Found`: Serviço não encontrado

---

## 🎨 Tipos de Campos (TipoCampo)

```typescript
enum TipoCampo {
  LOCALIZACAO = "Localização",
  FOTO = "Foto",
  DESCRICAO_DETALHADA = "Descrição Detalhada",
  DADOS_SOLICITANTE = "Dados do Solicitante",
  DATA_HORA = "Data/Hora",
  PLACA_VEICULO = "Placa de Veículo",
  NUMERO_IMOVEL = "Número do Imóvel",
  METRAGEM = "Metragem/Dimensões",
  VALOR_DECLARADO = "Valor Declarado",
  DOCUMENTOS_ANEXOS = "Documentos Anexos",
  NUMERO_PROTOCOLO_ANTERIOR = "Protocolo Anterior",
  OBSERVACOES = "Observações"
}
```

---

## 🚨 Tratamento de Erros

### Estrutura de Erro Padrão:
```json
{
  "timestamp": "2025-12-10T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Já existe um serviço com este título",
  "path": "/api/servicos"
}
```

### Códigos HTTP:
| Código | Significado |
|--------|-------------|
| 200 | OK - Sucesso |
| 201 | Created - Recurso criado |
| 204 | No Content - Deletado com sucesso |
| 400 | Bad Request - Validação falhou |
| 401 | Unauthorized - Token inválido/expirado |
| 403 | Forbidden - Sem permissão |
| 404 | Not Found - Recurso não encontrado |
| 409 | Conflict - Conflito (ex: título duplicado) |
| 500 | Internal Server Error - Erro no servidor |

---

## 🔧 Exemplo de Integração Angular

### Service Angular:

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ServicoService {
  
  private apiUrl = 'http://localhost:8080/api/servicos';

  constructor(private http: HttpClient) {}

  listarComFiltros(filtros: any): Observable<any> {
    let params = new HttpParams()
      .set('page', filtros.page || 0)
      .set('size', filtros.size || 10)
      .set('sort', filtros.sort || 'titulo')
      .set('direction', filtros.direction || 'asc');

    if (filtros.busca) {
      params = params.set('busca', filtros.busca);
    }

    if (filtros.categorias && filtros.categorias.length > 0) {
      filtros.categorias.forEach((cat: number) => {
        params = params.append('categorias', cat.toString());
      });
    }

    if (filtros.status && filtros.status.length > 0) {
      filtros.status.forEach((st: boolean) => {
        params = params.append('status', st.toString());
      });
    }

    return this.http.get<any>(this.apiUrl, { params });
  }

  buscarPorId(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}`);
  }

  criar(servico: any): Observable<any> {
    return this.http.post<any>(this.apiUrl, servico);
  }

  atualizar(id: number, servico: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${id}`, servico);
  }

  deletar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  toggleStatus(id: number): Observable<any> {
    return this.http.patch<any>(`${this.apiUrl}/${id}/toggle-status`, {});
  }

  listarAtivos(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/ativos`);
  }
}
```

---

## ✅ Checklist de Integração

- [x] API com paginação
- [x] Filtros por busca (LIKE)
- [x] Filtros por categorias (multiselect)
- [x] Filtros por status (multiselect)
- [x] Ordenação configurável
- [x] CRUD completo
- [x] Validações Bean Validation
- [x] Segurança JWT
- [x] CORS habilitado
- [x] HTTP codes adequados
- [x] Dados de categoria incluídos (ícone, cor)
- [x] Timestamps (createdAt, updatedAt)
- [x] Campos configuráveis por serviço

---

## 🎉 API 100% Pronta para Integração!

O módulo de serviços está completamente funcional e preparado para o frontend Angular. Todos os endpoints estão protegidos adequadamente e retornam os dados no formato esperado.

