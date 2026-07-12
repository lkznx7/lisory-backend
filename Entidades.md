# Documentação das Entidades do Banco de Dados

## Visão Geral

Este documento descreve todas as entidades do banco de dados do e-commerce de joias, incluindo sua finalidade, atributos e relacionamentos.

---

# 1. Users

## Descrição

A entidade **Users** armazena as informações dos usuários cadastrados no sistema.

É responsável pela autenticação e identificação dos clientes da loja.

Cada usuário pode:

- Realizar login.
- Possuir vários endereços.
- Possuir um carrinho de compras.
- Realizar diversos pedidos.
- Possuir vários Refresh Tokens.

---

## Campos

| Campo | Tipo | Obrigatório | Descrição |
|---------|------|------------|-----------|
| id | Integer | Sim | Identificador único do usuário |
| name | VARCHAR(255) | Sim | Nome completo |
| email | VARCHAR(255) | Sim | Email único utilizado para login |
| password | VARCHAR(255) | Sim | Senha criptografada |
| phone | VARCHAR(20) | Não | Telefone de contato |
| created_at | Timestamp | Sim | Data de criação |
| updated_at | Timestamp | Sim | Última atualização |

---

## Relacionamentos

| Relacionamento | Cardinalidade |
|----------------|---------------|
| Addresses | 1:N |
| Orders | 1:N |
| Refresh Tokens | 1:N |
| Cart | 1:1 |

---

# 2. Refresh Tokens

## Descrição

A entidade **Refresh Tokens** armazena os Refresh Tokens utilizados pela autenticação JWT.

Seu objetivo é permitir:

- Renovação do Access Token.
- Logout seguro.
- Revogação de sessões.
- Controle de blacklist.

---

## Campos

| Campo | Tipo | Descrição |
|--------|------|-----------|
| id | Integer | Identificador |
| user_id | FK | Usuário proprietário |
| subject | VARCHAR(255) | Subject presente no JWT |
| role | VARCHAR(50) | Papel do usuário |
| expiration | Timestamp | Data de expiração |
| black_list | Boolean | Indica se o token foi revogado |
| created_at | Timestamp | Data de criação |

---

## Relacionamentos

| Relacionamento | Cardinalidade |
|----------------|---------------|
| Users | N:1 |

---

# 3. Addresses

## Descrição

Representa os endereços cadastrados pelos usuários.

Esses endereços podem ser utilizados durante o checkout para entrega dos pedidos.

Um usuário pode possuir diversos endereços.

---

## Campos

| Campo | Tipo |
|--------|------|
| id | Integer |
| user_id | FK |
| street | VARCHAR(255) |
| number | VARCHAR(20) |
| complement | VARCHAR(255) |
| neighborhood | VARCHAR(255) |
| city | VARCHAR(255) |
| state | VARCHAR(100) |
| zip_code | VARCHAR(20) |
| created_at | Timestamp |

---

## Relacionamentos

| Relacionamento | Cardinalidade |
|----------------|---------------|
| Users | N:1 |
| Orders | 1:N |

---

# 4. Products

## Descrição

Armazena todos os produtos vendidos pela loja.

Neste projeto:

- Não existem variações.
- Cada produto possui apenas um preço.
- O SKU é opcional, porém único.

---

## Campos

| Campo | Tipo |
|--------|------|
| id | Integer |
| name | VARCHAR(255) |
| description | TEXT |
| sku | VARCHAR(100) |
| price | DECIMAL(10,2) |
| active | Boolean |
| created_at | Timestamp |
| updated_at | Timestamp |

---

## Relacionamentos

| Relacionamento | Cardinalidade |
|----------------|---------------|
| Product Images | 1:N |
| Cart Items | 1:N |
| Order Items | 1:N |

---

# 5. Product Images

## Descrição

Armazena todas as imagens dos produtos.

Um produto pode possuir várias imagens.

O campo **is_primary** identifica a imagem principal que será exibida na vitrine.

---

## Campos

| Campo | Tipo |
|--------|------|
| id | Integer |
| product_id | FK |
| image_url | TEXT |
| is_primary | Boolean |
| created_at | Timestamp |

---

## Relacionamentos

| Relacionamento | Cardinalidade |
|----------------|---------------|
| Products | N:1 |

---

# 6. Carts

## Descrição

Representa o carrinho de compras do usuário.

Cada usuário possui apenas um carrinho.

O carrinho armazena somente a estrutura principal, enquanto os produtos ficam na entidade Cart Items.

---

## Campos

| Campo | Tipo |
|--------|------|
| id | Integer |
| user_id | FK |
| created_at | Timestamp |
| updated_at | Timestamp |

---

## Relacionamentos

| Relacionamento | Cardinalidade |
|----------------|---------------|
| Users | 1:1 |
| Cart Items | 1:N |

---

# 7. Cart Items

## Descrição

Representa os produtos adicionados ao carrinho.

Cada registro corresponde a um produto presente no carrinho.

---

## Campos

| Campo | Tipo |
|--------|------|
| id | Integer |
| cart_id | FK |
| product_id | FK |
| quantity | Integer |

---

## Relacionamentos

| Relacionamento | Cardinalidade |
|----------------|---------------|
| Cart | N:1 |
| Products | N:1 |

---

# 8. Coupons

## Descrição

Representa os cupons promocionais da loja.

Um cupom pode conceder:

- Desconto percentual.
- Desconto em valor fixo.

Também controla:

- Quantidade máxima de utilizações.
- Quantidade utilizada.
- Expiração.
- Ativação/desativação.

---

## Campos

| Campo | Tipo |
|--------|------|
| id | Integer |
| code | VARCHAR(100) |
| discount_type | VARCHAR(50) |
| discount_value | DECIMAL(10,2) |
| max_uses | Integer |
| used_count | Integer |
| expiration | Timestamp |
| active | Boolean |
| created_at | Timestamp |

---

## Relacionamentos

| Relacionamento | Cardinalidade |
|----------------|---------------|
| Orders | 1:N |

---

# 9. Orders

## Descrição

A entidade **Orders** representa um pedido realizado pelo cliente.

É considerada a principal entidade do sistema.

Um pedido contém:

- Usuário.
- Endereço.
- Cupom.
- Produtos.
- Pagamento.
- Envio.
- Valores financeiros.

---

## Campos

| Campo | Tipo |
|--------|------|
| id | Integer |
| user_id | FK |
| address_id | FK |
| coupon_id | FK (Opcional) |
| status | VARCHAR(50) |
| subtotal | DECIMAL(10,2) |
| discount | DECIMAL(10,2) |
| shipping | DECIMAL(10,2) |
| total | DECIMAL(10,2) |
| created_at | Timestamp |
| updated_at | Timestamp |

---

## Relacionamentos

| Relacionamento | Cardinalidade |
|----------------|---------------|
| Users | N:1 |
| Addresses | N:1 |
| Coupons | N:1 |
| Order Items | 1:N |
| Payments | 1:1 |
| Shipments | 1:1 |

---

# 10. Order Items

## Descrição

Representa os produtos pertencentes a um pedido.

O preço unitário é armazenado para preservar o histórico caso o produto tenha alteração de preço futuramente.

---

## Campos

| Campo | Tipo |
|--------|------|
| id | Integer |
| order_id | FK |
| product_id | FK |
| quantity | Integer |
| unit_price | DECIMAL(10,2) |

---

## Relacionamentos

| Relacionamento | Cardinalidade |
|----------------|---------------|
| Orders | N:1 |
| Products | N:1 |

---

# 11. Payments

## Descrição

Armazena todas as informações relacionadas ao pagamento do pedido.

Integra-se ao Asaas.

Permite armazenar:

- Método de pagamento.
- Valor pago.
- Status.
- IDs externos da transação.

---

## Campos

| Campo | Tipo |
|--------|------|
| id | Integer |
| order_id | FK |
| asaas_payment_id | VARCHAR(255) |
| transaction_id | VARCHAR(255) |
| payment_method | VARCHAR(50) |
| amount | DECIMAL(10,2) |
| status | VARCHAR(50) |
| paid_at | Timestamp |
| created_at | Timestamp |
| updated_at | Timestamp |

---

## Relacionamentos

| Relacionamento | Cardinalidade |
|----------------|---------------|
| Orders | 1:1 |

---

# 12. Shipments

## Descrição

Responsável pelo gerenciamento da entrega dos pedidos.

Integra-se ao Melhor Envio.

Armazena:

- Transportadora.
- Serviço contratado.
- Código de rastreamento.
- Etiqueta.
- Datas de envio e entrega.

---

## Campos

| Campo | Tipo |
|--------|------|
| id | Integer |
| order_id | FK |
| melhor_envio_id | VARCHAR(255) |
| carrier | VARCHAR(255) |
| service | VARCHAR(255) |
| tracking_code | VARCHAR(255) |
| shipping_cost | DECIMAL(10,2) |
| status | VARCHAR(50) |
| label_url | TEXT |
| shipped_at | Timestamp |
| delivered_at | Timestamp |
| created_at | Timestamp |
| updated_at | Timestamp |

---

## Relacionamentos

| Relacionamento | Cardinalidade |
|----------------|---------------|
| Orders | 1:1 |

---

# Resumo Geral dos Relacionamentos

| Entidade | Relacionamentos |
|----------|-----------------|
| Users | Addresses, Orders, Refresh Tokens, Cart |
| Refresh Tokens | Users |
| Addresses | Users, Orders |
| Products | Product Images, Cart Items, Order Items |
| Product Images | Products |
| Cart | Users, Cart Items |
| Cart Items | Cart, Products |
| Coupons | Orders |
| Orders | Users, Addresses, Coupons, Order Items, Payments, Shipments |
| Order Items | Orders, Products |
| Payments | Orders |
| Shipments | Orders |

---

# Fluxo Geral do Sistema

```text
Usuário
    │
    ▼
Cadastro / Login
    │
    ▼
Carrinho
    │
    ▼
Adicionar Produtos
    │
    ▼
Aplicar Cupom (Opcional)
    │
    ▼
Checkout
    │
    ▼
Pedido
   ├──────────────┐
   │              │
Pagamento      Envio
   │              │
   └──────► Pedido Finalizado
```

---

# Diagrama Simplificado dos Relacionamentos

```text
Users
 ├───────────────< Addresses
 ├───────────────< Refresh Tokens
 ├───────────────< Orders >────────────── Coupons
 │                     │
 │                     ├──────────────< Order Items >──────── Products
 │                     ├────────────── Payments
 │                     └────────────── Shipments
 │
 └────────────── Cart ───────────────< Cart Items >────────── Products

Products
 └──────────────< Product Images
```

---

# Considerações Finais

Este modelo de dados foi projetado para atender às necessidades de um e-commerce de joias com foco em simplicidade e escalabilidade. A estrutura contempla autenticação baseada em JWT, gerenciamento de usuários e endereços, catálogo de produtos com múltiplas imagens, carrinho de compras, cupons de desconto, processamento de pedidos, integração com gateways de pagamento e serviços de envio, mantendo uma separação clara de responsabilidades entre as entidades e garantindo a integridade dos relacionamentos por meio de chaves estrangeiras.