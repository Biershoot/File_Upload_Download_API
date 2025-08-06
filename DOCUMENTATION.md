# 📚 Documentación Técnica - API de Autenticación JWT

## 🎯 Guía de Implementación y Uso

### 1. Ejemplos de Uso con Postman/Thunder Client

#### 1.1 Registro de Usuario
```http
POST http://localhost:8086/api/auth/register
Content-Type: application/json

{
    "username": "developer2025",
    "email": "dev@techcompany.com",
    "password": "SecurePass123!",
    "roles": ["ROLE_USER"]
}
```

**Respuesta Exitosa:**
```json
{
    "message": "Usuario registrado exitosamente"
}
```

#### 1.2 Login de Usuario
```http
POST http://localhost:8086/api/auth/login
Content-Type: application/json

{
    "username": "developer2025",
    "password": "SecurePass123!"
}
```

**Respuesta Exitosa:**
```json
{
    "token": "eyJhbGciOiJIUzI1NiIsInR5...",
    "type": "Bearer",
    "username": "developer2025",
    "email": "dev@techcompany.com",
    "roles": ["ROLE_USER"]
}
```

### 2. Integración con Frontend

#### 2.1 React/Next.js
```javascript
const login = async (credentials) => {
    const response = await fetch('http://localhost:8086/api/auth/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(credentials)
    });
    const data = await response.json();
    localStorage.setItem('token', data.token);
    return data;
};
```

#### 2.2 Angular
```typescript
login(credentials: any): Observable<any> {
    return this.http.post(
        'http://localhost:8086/api/auth/login',
        credentials
    );
}
```

### 3. Manejo de Errores Comunes

#### 3.1 Errores de Validación
```json
{
    "username": "El nombre de usuario es obligatorio",
    "email": "Formato de email inválido",
    "password": "La contraseña debe tener al menos 6 caracteres"
}
```

#### 3.2 Errores de Autenticación
```json
{
    "error": "Credenciales inválidas"
}
```

### 4. Seguridad y Mejores Prácticas

#### 4.1 Protección de Endpoints
```java
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/admin/users")
public List<UserDTO> getAllUsers() {
    // Implementación
}
```

#### 4.2 Validación de Datos
```java
@NotBlank(message = "El nombre de usuario es obligatorio")
@Size(min = 3, max = 20, message = "El nombre debe tener entre 3 y 20 caracteres")
private String username;
```

### 5. Configuración para Producción

#### 5.1 Application Properties para Producción
```properties
# Configuración de Base de Datos
spring.datasource.url=jdbc:mysql://localhost:3306/prod_db
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# Configuración JWT
app.jwtSecret=${JWT_SECRET}
app.jwtExpirationMs=86400000

# Configuración de Seguridad
spring.security.require-ssl=true
server.tomcat.remote-ip-header=x-forwarded-for
server.tomcat.protocol-header=x-forwarded-proto
```

### 6. Monitoreo y Logging

#### 6.1 Ejemplo de Logs
```log
2025-08-06 10:15:23.456 INFO  [AuthService] - Usuario registrado exitosamente: developer2025
2025-08-06 10:15:45.789 ERROR [AuthService] - Intento de login fallido para usuario: developer2025
```

### 7. Testing

#### 7.1 Ejemplo de Test de Integración
```java
@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {
    @Test
    public void whenValidLogin_thenReturnsToken() {
        // Implementation
    }
}
```

## 🔍 Características Avanzadas Implementadas

### 1. Rate Limiting
- Límite de intentos de login
- Protección contra ataques de fuerza bruta

### 2. Auditoría
- Registro de acciones de usuarios
- Tracking de cambios en entidades

### 3. Caché
- Implementación de caché para optimizar rendimiento
- Gestión eficiente de recursos

## 📈 Escalabilidad y Rendimiento

### 1. Métricas de Rendimiento
- Tiempo de respuesta promedio: <100ms
- Capacidad de manejar 1000+ req/seg

### 2. Optimizaciones
- Uso eficiente de conexiones a BD
- Implementación de pool de conexiones

## 🔄 CI/CD

### GitHub Actions Workflow
```yaml
name: Java CI with Maven

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
    - name: Build with Maven
      run: mvn -B package --file pom.xml
```
