# 🔐 Sistema de Autenticação Moderno

Sistema completo com **Access Token** (curta duração) + **Refresh Token** (longa duração persistido).

---

## 📋 Configurações

### application.properties

```properties
# JWT Configuration
jwt.secret=ConnectaGestorSecretKey2024MinhaChaveSuperSecretaParaJWT
jwt.access-token.expiration=900000      # 15 minutos (900.000 ms)
jwt.refresh-token.expiration=2592000000 # 30 dias (2.592.000.000 ms)
```

---

## 🎯 Fluxo de Autenticação

### 1. Login (Obter Tokens)

**Endpoint:** `POST /api/auth/login`

**Request:**
```json
{
  "email": "lucaspenna@wimagroup.com.br",
  "senha": "admin0946"
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "tipo": "Bearer",
  "expiresIn": 900,
  "email": "lucaspenna@wimagroup.com.br",
  "nome": "Lucas Penna",
  "role": "ROLE_SUPER_ADMIN"
}
```

**Detalhes:**
- `accessToken`: JWT para autenticar requisições (válido por 15 minutos)
- `refreshToken`: UUID para renovar tokens (válido por 30 dias)
- `expiresIn`: Tempo de expiração do accessToken em segundos
- Ambos os tokens são retornados e devem ser armazenados no frontend

---

### 2. Refresh (Renovar Tokens)

**Endpoint:** `POST /api/auth/refresh`

**Request:**
```json
{
  "refreshToken": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9... (NOVO)",
  "refreshToken": "f9e8d7c6-b5a4-3210-fedc-ba0987654321 (NOVO)",
  "tipo": "Bearer",
  "expiresIn": 900,
  "email": "lucaspenna@wimagroup.com.br",
  "nome": "Lucas Penna",
  "role": "ROLE_SUPER_ADMIN"
}
```

**Detalhes:**
- Renova AMBOS os tokens (access e refresh)
- O refreshToken antigo é atualizado no banco
- Deve ser chamado quando o accessToken expirar
- Não requer Authorization header

---

### 3. Logout (Invalidar Tokens)

**Endpoint:** `POST /api/auth/logout`

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Request:**
```json
{
  "refreshToken": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

**Response:**
```json
"Logout realizado com sucesso!"
```

**Detalhes:**
- Marca o refreshToken como revogado no banco
- Requer autenticação (accessToken válido)
- Após logout, o refreshToken não pode mais ser usado

---

## 🛠️ Uso no Frontend Angular

### Armazenamento

```typescript
// Após login/refresh
localStorage.setItem('accessToken', response.accessToken);
localStorage.setItem('refreshToken', response.refreshToken);
localStorage.setItem('tokenExpiration', Date.now() + (response.expiresIn * 1000));
```

### HTTP Interceptor

```typescript
import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError, BehaviorSubject } from 'rxjs';
import { catchError, filter, take, switchMap } from 'rxjs/operators';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  
  private isRefreshing = false;
  private refreshTokenSubject: BehaviorSubject<any> = new BehaviorSubject<any>(null);

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    
    // Não adicionar token em rotas públicas
    if (this.isPublicRoute(request.url)) {
      return next.handle(request);
    }

    // Adicionar accessToken
    const accessToken = localStorage.getItem('accessToken');
    if (accessToken) {
      request = this.addToken(request, accessToken);
    }

    return next.handle(request).pipe(
      catchError(error => {
        if (error instanceof HttpErrorResponse && error.status === 401) {
          return this.handle401Error(request, next);
        }
        return throwError(() => error);
      })
    );
  }

  private addToken(request: HttpRequest<any>, token: string) {
    return request.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  private handle401Error(request: HttpRequest<any>, next: HttpHandler) {
    if (!this.isRefreshing) {
      this.isRefreshing = true;
      this.refreshTokenSubject.next(null);

      const refreshToken = localStorage.getItem('refreshToken');
      
      if (refreshToken) {
        return this.authService.refresh(refreshToken).pipe(
          switchMap((response: any) => {
            this.isRefreshing = false;
            
            localStorage.setItem('accessToken', response.accessToken);
            localStorage.setItem('refreshToken', response.refreshToken);
            localStorage.setItem('tokenExpiration', Date.now() + (response.expiresIn * 1000));
            
            this.refreshTokenSubject.next(response.accessToken);
            return next.handle(this.addToken(request, response.accessToken));
          }),
          catchError((err) => {
            this.isRefreshing = false;
            this.authService.logout();
            return throwError(() => err);
          })
        );
      }
    }

    return this.refreshTokenSubject.pipe(
      filter(token => token !== null),
      take(1),
      switchMap((token) => next.handle(this.addToken(request, token)))
    );
  }

  private isPublicRoute(url: string): boolean {
    const publicRoutes = ['/api/auth/login', '/api/auth/refresh', '/api/auth/recovery-password', '/api/auth/reset-password'];
    return publicRoutes.some(route => url.includes(route));
  }
}
```

### Auth Service

```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  
  private apiUrl = 'http://localhost:8080/api/auth';

  constructor(
    private http: HttpClient,
    private router: Router
  ) {}

  login(email: string, senha: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/login`, { email, senha });
  }

  refresh(refreshToken: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/refresh`, { refreshToken });
  }

  logout(): void {
    const refreshToken = localStorage.getItem('refreshToken');
    
    if (refreshToken) {
      this.http.post(`${this.apiUrl}/logout`, { refreshToken }).subscribe();
    }
    
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('tokenExpiration');
    this.router.navigate(['/login']);
  }

  isAuthenticated(): boolean {
    const accessToken = localStorage.getItem('accessToken');
    const expiration = localStorage.getItem('tokenExpiration');
    
    if (!accessToken || !expiration) {
      return false;
    }
    
    return Date.now() < parseInt(expiration);
  }
}
```

---

## 🗄️ Estrutura do Banco de Dados

### Tabela: refresh_tokens

```sql
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(500) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

---

## 🔒 Segurança

### Access Token (JWT)
- ✅ Duração curta (15 minutos)
- ✅ Contém: email, role, tipo (access)
- ✅ Assinado com HS512
- ✅ Não persistido no banco
- ✅ Validado em cada requisição

### Refresh Token
- ✅ Duração longa (30 dias)
- ✅ UUID aleatório
- ✅ Persistido no banco de dados
- ✅ Pode ser revogado (logout)
- ✅ Renovado a cada refresh

### CORS
- ✅ Origem: `http://localhost:4200`
- ✅ Métodos: GET, POST, PUT, PATCH, DELETE, OPTIONS
- ✅ Headers: Authorization, Content-Type, Accept
- ✅ Credenciais: Habilitadas
- ✅ OPTIONS liberado antes do filtro JWT

---

## 📡 Endpoints Protegidos

### Rotas Públicas (sem autenticação)
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/recovery-password`
- `POST /api/auth/reset-password`
- `GET /api/categorias/ativas`
- `GET /api/servicos/ativos`
- `GET /api/servicos/categoria/**`
- `POST /api/protocolos` (criação de protocolo público)

### Rotas Protegidas (requerem accessToken)
- `GET /api/auth/me`
- `POST /api/auth/change-password`
- `POST /api/auth/logout`
- Todos os endpoints de administração

---

## 🧪 Testando

### 1. Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "lucaspenna@wimagroup.com.br",
    "senha": "admin0946"
  }'
```

### 2. Acessar Rota Protegida
```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer {accessToken}"
```

### 3. Refresh Token
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "{seu-refresh-token}"
  }'
```

### 4. Logout
```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer {accessToken}" \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "{seu-refresh-token}"
  }'
```

---

## ⚠️ Tratamento de Erros

### Token Expirado (401)
```json
{
  "timestamp": "2025-12-10T12:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Token expirado"
}
```
**Ação:** Chamar `/api/auth/refresh` com o refreshToken

### Refresh Token Inválido/Revogado (401)
```json
{
  "timestamp": "2025-12-10T12:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Refresh token inválido"
}
```
**Ação:** Redirecionar para login

### Usuário Inativo (401)
```json
{
  "timestamp": "2025-12-10T12:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Usuário inativo"
}
```
**Ação:** Exibir mensagem e redirecionar para login

---

## 📝 Checklist de Integração

- [ ] Instalar HTTP Interceptor no Angular
- [ ] Armazenar tokens no localStorage após login
- [ ] Adicionar accessToken no header Authorization
- [ ] Implementar lógica de refresh automático ao receber 401
- [ ] Implementar logout que chama API e limpa localStorage
- [ ] Adicionar guard de rota verificando isAuthenticated()
- [ ] Testar fluxo completo: login → requisições → refresh → logout
- [ ] Tratar erros de rede e tokens inválidos
- [ ] Implementar exibição de tempo restante de sessão (opcional)

---

## ✅ Status

**Backend totalmente pronto para produção!**

- ✅ Access Token JWT (15 min)
- ✅ Refresh Token persistido (30 dias)
- ✅ Login retorna ambos tokens
- ✅ Endpoint de refresh renovando tokens
- ✅ Endpoint de logout invalidando tokens
- ✅ Filtro JWT validando accessToken
- ✅ CORS configurado para localhost:4200
- ✅ OPTIONS liberado para preflight
- ✅ Testes de compilação OK

**Pronto para integração com Angular!** 🚀

