# API Reference — LISORY Admin

Este documento descreve todos os endpoints REST do painel administrativo da LISORY.

---

## Sumário

1. [Autenticação](#1-autenticação)
2. [Dashboard](#2-dashboard)
3. [Produtos](#3-produtos)
4. [Categorias](#4-categorias)
5. [Coleções](#5-coleções)
6. [Pedidos](#6-pedidos)
7. [Clientes](#7-clientes)
8. [Cupons](#8-cupons)
9. [Configurações da Loja](#9-configurações-da-loja)
10. [Perfil do Administrador](#10-perfil-do-administrador)

---

## 1. Autenticação

### `POST /api/auth/login`
- **Função:** Autentica o administrador e retorna um token JWT
- **Body:**
  ```json
  { "email": "string", "password": "string" }
  ```

### `POST /api/auth/logout`
- **Função:** Invalida o token do administrador
- **Headers:** `Authorization: Bearer <token>`

### `GET /api/auth/me`
- **Função:** Retorna os dados do administrador logado
- **Headers:** `Authorization: Bearer <token>`

---

## 2. Dashboard

### `GET /api/admin/dashboard/stats`
- **Função:** Retorna as estatísticas principais (faturamento, pedidos, produtos, clientes)

### `GET /api/admin/dashboard/sales`
- **Função:** Retorna dados de vendas por mês para o gráfico

### `GET /api/admin/dashboard/recent-orders?limit=5`
- **Função:** Retorna os pedidos mais recentes

### `GET /api/admin/dashboard/top-products?limit=5`
- **Função:** Retorna os produtos mais vendidos

### `GET /api/admin/dashboard/recent-customers?limit=5`
- **Função:** Retorna os clientes mais recentes

---

## 3. Produtos

Produtos não possuem variações (cor, tamanho, modelo). Estoque não é controlado pelo sistema. O painel cadastra preço normal e preço promocional.

**Garantia:**
- Peças douradas: 6 meses contra defeitos de fabricação
- Peças prata: 1 ano contra defeitos de fabricação

### `GET /api/admin/products`
- **Função:** Lista todos os produtos
- **Query params:** `?search=&category=&status=&page=1&limit=10`

### `GET /api/admin/products/:id`
- **Função:** Retorna um produto pelo ID

### `POST /api/admin/products`
- **Função:** Cria um novo produto

### `PUT /api/admin/products/:id`
- **Função:** Atualiza um produto existente

### `DELETE /api/admin/products/:id`
- **Função:** Exclui um produto

---

## 4. Categorias

### `GET /api/admin/categories`
- **Função:** Lista todas as categorias

### `GET /api/admin/categories/:id`
- **Função:** Retorna uma categoria pelo ID

### `POST /api/admin/categories`
- **Função:** Cria uma nova categoria

### `PUT /api/admin/categories/:id`
- **Função:** Atualiza uma categoria

### `DELETE /api/admin/categories/:id`
- **Função:** Exclui uma categoria

---

## 5. Coleções

### `GET /api/admin/collections`
- **Função:** Lista todas as coleções (agrupamentos temáticos de produtos)

---

## 6. Pedidos

**Gateway:** Infinity Pay (PIX | Cartão)
**Frete:** Calculado pelo Melhor Envio
**Status:** `AGUARDANDO_PAGAMENTO` → `PAGO` (atualizado automaticamente pelo Melhor Envio)
**Cancelamento:** Cliente não pode cancelar — deve entrar em contato com o suporte.

### `GET /api/admin/orders`
- **Função:** Lista todos os pedidos
- **Query params:** `?search=&status=&page=1&limit=10`

### `GET /api/admin/orders/:id`
- **Função:** Retorna um pedido pelo ID

---

## 7. Clientes

Conta não é obrigatória para compra. Checkout como visitante armazena ao menos nome e contato.

### `GET /api/admin/customers`
- **Função:** Lista todos os clientes
- **Query params:** `?search=&page=1&limit=10`

### `GET /api/admin/customers/:id`
- **Função:** Retorna um cliente pelo ID

### `GET /api/admin/customers/top?limit=5`
- **Função:** Retorna os clientes que mais gastaram

---

## 8. Cupons

Cupom inicial: **Lisory10** (primeira compra).

### `GET /api/admin/coupons`
- **Função:** Lista todos os cupons
- **Query params:** `?status=active|expired|scheduled|exhausted|inactive`

### `GET /api/admin/coupons/:id`
- **Função:** Retorna um cupom pelo ID

### `POST /api/admin/coupons`
- **Função:** Cria um novo cupom

### `PUT /api/admin/coupons/:id`
- **Função:** Atualiza um cupom

### `DELETE /api/admin/coupons/:id`
- **Função:** Exclui um cupom

### `POST /api/admin/coupons/:id/toggle`
- **Função:** Ativa/desativa um cupom (alterna `isActive`)

### `POST /api/admin/coupons/:id/duplicate`
- **Função:** Duplica um cupom com sufixo `-COPY` e `isActive: false`

### `GET /api/admin/coupons/:id/status`
- **Função:** Retorna o status calculado do cupom
- **Status possíveis:** `active | expired | scheduled | exhausted | inactive`

---

## 9. Configurações da Loja

**Canais de contato:** WhatsApp | Instagram | E-mail

### `GET /api/admin/settings`
- **Função:** Retorna as configurações da loja

### `PUT /api/admin/settings`
- **Função:** Atualiza as configurações da loja

---

## 10. Perfil do Administrador

### `GET /api/admin/profile`
- **Função:** Retorna os dados do administrador logado
- **Headers:** `Authorization: Bearer <token>`

### `PUT /api/admin/profile`
- **Função:** Atualiza os dados do administrador

### `PUT /api/admin/profile/avatar`
- **Função:** Atualiza a foto do administrador (upload de arquivo)
- **Body:** `multipart/form-data` com campo `avatar`

### `PUT /api/admin/profile/password`
- **Função:** Altera a senha do administrador
- **Body:**
  ```json
  { "currentPassword": "string", "newPassword": "string" }
  ```
