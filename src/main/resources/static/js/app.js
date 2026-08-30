// Mensagens, validações e configurações utilizadas pelo sistema
const CONFIRM_DISCARD_CHANGES = "Deseja realmente descartar as alterações?";
const CONFIRM_DELETE = "Deseja realmente excluir?";
const CONFIRM_UPDATE_ORDER_DATE = "Deseja atualizar a data e hora do pedido?";
const CONFIRM_RESET_STATUS = "Deseja voltar o status do pedido para Pendente?";
const ORDER_ITEM_REQUIRED = "Preencha o campo Item(ns) do pedido.";
const PHONE_REQUIRED = "Preencha o campo telefone.";

const VALIDATION_MESSAGES = {
    requiredGeneric: "Preencha este campo.",
    requiredClientName: "Preencha o campo Nome do cliente.",
    requiredProductName: "Preencha o campo Nome do item.",
    requiredProductCategory: "Preencha o campo Categoria.",
    requiredProductPrice: "Preencha o campo Preço.",
    requiredOrderClient: "Preencha o campo Nome do cliente.",
    requiredOrderPayment: "Preencha o campo Forma de pagamento.",
    orderItemRequired: "Preencha o campo Item(ns) do pedido.",
    orderQuantityRequired: "Preencha o campo Quantidade.",
    phoneRequired: "Preencha o campo telefone.",
    phoneInvalid: "Impossível criar/editar cliente, Telefone inválido. Use apenas números, +, (, ), - e espaço.",
    phoneMin: "Impossível criar/editar clientes, Telefone deve possuir ao menos 8 caracteres",
    phoneMax: "Impossível criar/editar clientes, Telefone deve possuir até 25 caracteres",
    invalidNumber: "Informe um número válido.",
    invalidMinimum: "Quantidade do item deve ser maior que zero."
};

const CATEGORY_EMOJIS = {
    "Espetinho Tradicional": "🍖",
    "Espetinho Completo": "🍖",
    "Bebida": "🥤",
    "Lanches": "🍔"
};

// Inicialização das funcionalidades da aplicação
document.addEventListener("DOMContentLoaded", () => {
    initPortugueseValidationMessages();
    storeInitialModalContent();
    initClickActions();
    initSubmitActions();
    initInputAndChangeActions();
    initDateMasks();
    initOrderForms();
    initCategoryEmojiFields();
	initSaveOrderStateBeforeClientRedirect();
	initOpenModalByUrl();
	initRestoreOrderStateFromUrl();
	initAutoHideMessages();
	
});

// Validação e personalização das mensagens dos formulários
function initPortugueseValidationMessages() {
    document.addEventListener("invalid", (event) => {
        const field = event.target;

        if (field.validity.customError) {
            return;
        }

        field.setCustomValidity(getPortugueseValidationMessage(field));
    }, true);

    document.addEventListener("input", (event) => {
        if (event.target.matches("input, select, textarea")) {
            event.target.setCustomValidity("");
        }
    });

    document.addEventListener("change", (event) => {
        if (event.target.matches("input, select, textarea")) {
            event.target.setCustomValidity("");
        }
    });
}

function getPortugueseValidationMessage(field) {
    const name = field.name || "";
    const placeholder = field.getAttribute("placeholder") || "";

    if (field.validity?.valueMissing) {
        if (name === "nomeCliente") return VALIDATION_MESSAGES.requiredClientName;
        if (name === "nomeProduto") return VALIDATION_MESSAGES.requiredProductName;
        if (name === "idCategoria") return VALIDATION_MESSAGES.requiredProductCategory;
        if (name === "precoUnitario") return VALIDATION_MESSAGES.requiredProductPrice;
        if (name === "idCliente") return VALIDATION_MESSAGES.requiredOrderClient;
        if (name === "idPagamento" || name === "nomeFormaPagamento") return VALIDATION_MESSAGES.requiredOrderPayment;
        if (name === "produtos") return VALIDATION_MESSAGES.orderItemRequired;
        if (name === "quantidades") return VALIDATION_MESSAGES.orderQuantityRequired;
        if (name === "telefones") return VALIDATION_MESSAGES.phoneRequired;
        return VALIDATION_MESSAGES.requiredGeneric;
    }

    if (name === "telefones" && field.value.trim() !== "" && field.validity?.patternMismatch) {return VALIDATION_MESSAGES.phoneInvalid;}
    if (name === "telefones" && field.validity?.tooShort) return VALIDATION_MESSAGES.phoneMin;
    if (name === "telefones" && field.validity?.tooLong) return VALIDATION_MESSAGES.phoneMax;
    if (field.validity?.badInput) return VALIDATION_MESSAGES.invalidNumber;
	if (field.validity?.rangeUnderflow) {if (name === "precoUnitario") {return "Preço deve ser maior que 0.";}return VALIDATION_MESSAGES.invalidMinimum;}
    if (field.validity?.patternMismatch && field.title) return field.title;
    if (placeholder) return VALIDATION_MESSAGES.requiredGeneric;

    return VALIDATION_MESSAGES.requiredGeneric;
}

// Controle e gerenciamento dos modais
function storeInitialModalContent() {
    document.querySelectorAll(".modal").forEach((modal) => {
        modal.dataset.initialHtml = modal.innerHTML;
    });
}

// Eventos de clique da aplicação
function initClickActions() {
    document.addEventListener("click", (event) => {
        const modalButton = event.target.closest("[data-modal-target]");
        if (modalButton) {
            const modal = document.getElementById(modalButton.dataset.modalTarget);
            openModal(modal);
            return;
        }

        const closeButton = event.target.closest("[data-close-modal]");
        if (closeButton) {
            closeModal(closeButton.closest(".modal"));
            return;
        }

        const addPhoneButton = event.target.closest("[data-add-phone]");
        if (addPhoneButton) {
            addPhoneField(addPhoneButton);
            return;
        }

        const addOrderItemButton = event.target.closest("[data-add-order-item]");
        if (addOrderItemButton) {
            addOrderItemRow(addOrderItemButton);
            return;
        }

        const removeButton = event.target.closest("[data-remove-field]");
        if (removeButton) {
            removeDynamicField(removeButton);
        }
    });

    document.querySelectorAll(".modal").forEach((modal) => {
        modal.addEventListener("click", (event) => {
            if (event.target === modal) {
                closeModal(modal);
            }
        });
    });
}

// Validações executadas durante o envio dos formulários
function initSubmitActions() {
    document.addEventListener("submit", (event) => {
        const form = event.target;

		const deleteForm = form.closest("[data-confirm-delete]");

		if (deleteForm) {
		    const possuiPedidos = deleteForm.dataset.hasOrders === "true";

		    if (possuiPedidos) {
		        event.preventDefault();

		        alert("Este cliente possui pedidos vinculados a ele. Exclua os pedidos antes de excluir o cliente.");

		        return;
		    }

		    if (!confirm(CONFIRM_DELETE)) {
		        event.preventDefault();
		        return;
		    }
		}

        const clientForm = form.closest("[data-client-form]");
        if (clientForm) {
			const campoNomeCliente = clientForm.querySelector('input[name="nomeCliente"]');

			if (campoNomeCliente && campoNomeCliente.value.trim() === "") {
			    event.preventDefault();

			    campoNomeCliente.setCustomValidity(VALIDATION_MESSAGES.requiredClientName);
			    campoNomeCliente.reportValidity();
			    campoNomeCliente.focus();

			    return;
			}
			
    const telefones = Array.from(
        clientForm.querySelectorAll('input[name="telefones"]')
    );

    telefones.forEach(input => input.setCustomValidity(""));

    const telefonesPreenchidos = telefones.filter(
        input => input.value.trim() !== ""
    );

    if (telefonesPreenchidos.length === 0) {
        event.preventDefault();

        const primeiroTelefone = telefones[0];

        if (primeiroTelefone) {
            primeiroTelefone.setCustomValidity(VALIDATION_MESSAGES.phoneRequired);
            primeiroTelefone.reportValidity();
            primeiroTelefone.focus();
        }

        return;
    }

    for (const telefone of telefonesPreenchidos) {
        const valor = telefone.value.trim();

        if (!/^[0-9+() \-]+$/.test(valor)) {
            event.preventDefault();

            telefone.setCustomValidity(VALIDATION_MESSAGES.phoneInvalid);
            telefone.reportValidity();
            telefone.focus();

            return;
        }

        if (valor.length < 8) {
            event.preventDefault();

            telefone.setCustomValidity(VALIDATION_MESSAGES.phoneMin);
            telefone.reportValidity();
            telefone.focus();

            return;
        }

        if (valor.length > 25) {
            event.preventDefault();

            telefone.setCustomValidity(VALIDATION_MESSAGES.phoneMax);
            telefone.reportValidity();
            telefone.focus();

            return;
        }
    }
}
const campoNomeProduto = form.querySelector('input[name="nomeProduto"]');

if (campoNomeProduto && campoNomeProduto.value.trim() === "") {
    event.preventDefault();

    campoNomeProduto.setCustomValidity(VALIDATION_MESSAGES.requiredProductName);
    campoNomeProduto.reportValidity();
    campoNomeProduto.focus();

    return;
}
const campoPreco = form.querySelector('input[name="precoUnitario"]');

if (campoPreco) {
    campoPreco.setCustomValidity("");

    const valorPreco = Number(campoPreco.value);

    if (campoPreco.value.trim() !== "" && valorPreco <= 0) {
        event.preventDefault();

        campoPreco.setCustomValidity("Preço deve ser maior que 0.");
        campoPreco.reportValidity();
        campoPreco.focus();

        return;
    }
}
        const orderForm = form.closest("[data-order-form]");
        if (orderForm) {
            if (!hasAtLeastOneSelectedOrderItem(orderForm)) {
                event.preventDefault();
                alert(ORDER_ITEM_REQUIRED);
                return;
            }

            if (orderForm.matches("[data-order-edit-form]")) {
                handleEditOrderPrompts(orderForm);
            }
        }
    });
}

// Tratamento de alterações realizadas nos campos
function initInputAndChangeActions() {
    document.addEventListener("input", (event) => {
        const dateInput = event.target.closest("[data-date-mask]");
        if (dateInput) {
            applyDateMask(dateInput);
        }

        const orderForm = event.target.closest("[data-order-form]");
        if (orderForm) {
            updateOrderFormTotal(orderForm);
        }
    });

    document.addEventListener("change", (event) => {
        const categorySelect = event.target.closest("[data-category-select]");
        if (categorySelect) {
            updateCategoryEmoji(categorySelect);
        }


        const orderForm = event.target.closest("[data-order-form]");
        if (orderForm) {
            updateOrderFormTotal(orderForm);
        }
    });
}

// Abertura, fechamento e restauração dos modais
function openModal(modal) {
    if (!modal) return;

    modal.classList.add("open");
    prepareForms(modal);
    updateOrderFormsInside(modal);
    initCategoryEmojiFields(modal);
}

function closeModal(modal) {
    if (!modal) return;

    if (modalHasChanges(modal) && !confirm(CONFIRM_DISCARD_CHANGES)) {
        return;
    }

	if (modal.id === "modalNovoCliente" && deveVoltarParaPedido()) {
	    window.location.href = montarUrlRetornoPedido();
	    return;
	}

	if (modal.id === "modalNovoCliente") {
	    limparFormularioNovoCliente(modal);
	} else if (modalHasChanges(modal)) {
	    resetModalContent(modal);
	}

    modal.classList.remove("open");
}

function resetModalContent(modal) {
    if (modal?.dataset.initialHtml) {
        modal.innerHTML = modal.dataset.initialHtml;
    }
}

function limparFormularioNovoCliente(modal) {
    const form = modal.querySelector("[data-client-form]");

    if (!form) return;

    form.reset();

    form.querySelectorAll("input, select, textarea").forEach((field) => {
        field.setCustomValidity("");
    });

    const phoneList = form.querySelector("[data-phone-list]");

    if (phoneList) {
        const phoneRows = phoneList.querySelectorAll(".form-inline");

        phoneRows.forEach((row, index) => {
            if (index > 0) {
                row.remove();
            }
        });
    }

    prepareForms(modal);
}

function modalHasChanges(modal) {
    const forms = modal.querySelectorAll("form");

    for (const form of forms) {
        if (!form.dataset.originalState) {
            continue;
        }

        if (form.dataset.originalState !== serializeForm(form)) {
            return true;
        }
    }

    return false;
}

// Controle de alterações realizadas nos formulários
function prepareForms(scope = document) {
    scope.querySelectorAll("form").forEach((form) => {
        form.dataset.originalState = serializeForm(form);

        if (form.matches("[data-order-edit-form]")) {
            form.dataset.originalOrderNonStatus = serializeOrderNonStatus(form);
            form.dataset.originalOrderStatus = form.querySelector("[data-status-field]")?.value || "";
        }

        const firstOrderRow = form.querySelector("[data-order-row]");
        if (firstOrderRow) {
            form.dataset.orderRowTemplate = firstOrderRow.outerHTML;
        }
    });
}

function serializeForm(form) {
    const values = [];

    form.querySelectorAll("input, select, textarea").forEach((field) => {
        if (!field.name || field.type === "submit" || field.type === "button") {
            return;
        }

        values.push(`${field.name}=${field.value}`);
    });

    return JSON.stringify(values);
}

function serializeOrderNonStatus(form) {
    const values = [];

    form.querySelectorAll("input, select, textarea").forEach((field) => {
        if (!field.name || field.name === "nomeStatus" || field.name === "atualizarDataHoraPedido" || field.name === "voltarStatusPendente") {
            return;
        }

        values.push(`${field.name}=${field.value}`);
    });

    return JSON.stringify(values);
}

function handleEditOrderPrompts(form) {
    const originalNonStatus = form.dataset.originalOrderNonStatus || serializeOrderNonStatus(form);
    const currentNonStatus = serializeOrderNonStatus(form);
    const nonStatusChanged = originalNonStatus !== currentNonStatus;
    const updateDateField = form.querySelector("[data-update-date-field]");
    const resetStatusField = form.querySelector("[data-reset-status-field]");

    if (updateDateField) {
        updateDateField.value = "false";
    }

    if (resetStatusField) {
        resetStatusField.value = "false";
    }

    if (nonStatusChanged) {
        if (updateDateField && confirm(CONFIRM_UPDATE_ORDER_DATE)) {
            updateDateField.value = "true";
        }

        if (resetStatusField && confirm(CONFIRM_RESET_STATUS)) {
            resetStatusField.value = "true";
        }
    }
}

// Gerenciamento dinâmico de telefones e itens dos pedidos
function addPhoneField(button) {
    const formGroup = button.closest(".form-group");
    const fields = formGroup?.querySelector("[data-phone-list]") || formGroup?.querySelector(".dynamic-fields");

    if (!fields) return;

    const field = document.createElement("div");
    field.className = "form-inline";
    field.dataset.createdRow = "true";
    field.innerHTML = `
	    <input class="input" type="text" name="telefones" placeholder="Insira outro telefone aqui"
	        minlength="8" maxlength="25" pattern="[0-9+() \\-]+"
	        title="Use apenas números, +, (, ), - e espaço.">
	    <button class="btn btn-primary" type="button" data-remove-field>-</button>
	`;

    fields.appendChild(field);
}

function addOrderItemRow(button) {
    const form = button.closest("[data-order-form]");
    const fields = button.closest(".form-group")?.querySelector(".dynamic-fields");

    if (!form || !fields) return;

    let newRow;

    if (form.dataset.orderRowTemplate) {
        const template = document.createElement("template");
        template.innerHTML = form.dataset.orderRowTemplate.trim();
        newRow = template.content.firstElementChild;
    } else {
        const firstRow = fields.querySelector("[data-order-row]");
        if (!firstRow) return;
        newRow = firstRow.cloneNode(true);
    }

    newRow.dataset.createdRow = "true";
    newRow.querySelector("[data-order-product]").value = "";
    newRow.querySelector("[data-order-qty]").value = 1;

    let actionButton = newRow.querySelector("button");

    if (!actionButton) {
        actionButton = document.createElement("button");
        actionButton.className = "btn btn-primary";
        actionButton.type = "button";
        newRow.appendChild(actionButton);
    }

    actionButton.textContent = "-";
    actionButton.removeAttribute("data-add-order-item");
    actionButton.setAttribute("data-remove-field", "");

    fields.appendChild(newRow);
    updateOrderFormTotal(form);
}

function removeDynamicField(button) {
    const clientForm = button.closest("[data-client-form]");
    const orderForm = button.closest("[data-order-form]");
    const field = button.closest(".form-inline");

    if (!field) return;

    // CLIENTE: nunca remove o último campo de telefone
    if (clientForm) {
        const phoneFields = clientForm.querySelectorAll('input[name="telefones"]');

        if (phoneFields.length <= 1) {
            const input = field.querySelector('input[name="telefones"]');

            if (input) {
                input.value = "";
                input.setCustomValidity("");
                input.focus();
            }

            return;
        }
    }

    field.remove();

    if (orderForm) {
        updateOrderFormTotal(orderForm);
    }
}

// Controle de categorias e emojis do cardápio
function initCategoryEmojiFields(scope = document) {
    scope.querySelectorAll("[data-category-select]").forEach((select) => {
        updateCategoryEmoji(select);
    });
}

function updateCategoryEmoji(select) {
    const formGroup = select.closest(".form-group");
    const emojiPreview = formGroup?.querySelector("[data-category-emoji]");

    if (!emojiPreview) return;

    const selectedText = select.options[select.selectedIndex]?.text || "";
    emojiPreview.textContent = select.value ? (CATEGORY_EMOJIS[selectedText] || "") : "";
}

// Controle e cálculo dos pedidos
function initOrderForms() {
    document.querySelectorAll("[data-order-form]").forEach((form) => {
        const firstOrderRow = form.querySelector("[data-order-row]");
        if (firstOrderRow) {
            form.dataset.orderRowTemplate = firstOrderRow.outerHTML;
        }

        updateOrderFormTotal(form);
    });
}

function updateOrderFormsInside(scope) {
    scope.querySelectorAll("[data-order-form]").forEach((form) => {
        const firstOrderRow = form.querySelector("[data-order-row]");
        if (firstOrderRow) {
            form.dataset.orderRowTemplate = firstOrderRow.outerHTML;
        }

        updateOrderFormTotal(form);
    });
}

function updateOrderFormTotal(form) {
    if (!form) return;

    let total = 0;

    form.querySelectorAll("[data-order-row]").forEach((row) => {
        const productSelect = row.querySelector("[data-order-product]");
        const quantityInput = row.querySelector("[data-order-qty]");

        const selectedOption = productSelect?.options[productSelect.selectedIndex];
        const price = Number(String(selectedOption?.dataset.price || "0").replace(",", "."));
        const quantity = Number(quantityInput?.value || 0);

        total += price * quantity;
    });

    const totalElement = form.querySelector("[data-order-form-total]");

    if (totalElement) {
        totalElement.textContent = formatMoneyBR(total);
    }
}

function hasAtLeastOneSelectedOrderItem(form) {
    return Array.from(form.querySelectorAll("[data-order-product]"))
        .some((select) => select.value && select.value.trim() !== "");
}

function hasAtLeastOnePhone(form) {
    return Array.from(form.querySelectorAll('input[name="telefones"]'))
        .some((input) => input.value && input.value.trim() !== "");
}

// Formatação de valores monetários
function formatMoneyBR(value) {
    return Number(value || 0).toLocaleString("pt-BR", {
        style: "currency",
        currency: "BRL"
    });
}

// Máscara de datas utilizada nos filtros e formulários
function initDateMasks() {
    document.querySelectorAll("[data-date-mask]").forEach((input) => {
        applyDateMask(input);
    });
}

function applyDateMask(input) {
    let value = input.value.replace(/\D/g, "");

    value = value.replace(/(\d{2})(\d)/, "$1/$2");
    value = value.replace(/(\d{2})\/(\d{2})(\d)/, "$1/$2/$3");

    input.value = value.slice(0, 10);
}

// Controle de abertura automática de modais pela URL
function initOpenModalByUrl() {
    const params = new URLSearchParams(window.location.search);

    if (params.get("novoCliente") === "true") {
        openModal(document.getElementById("modalNovoCliente"));
    }

    if (params.get("novoPedido") === "true") {
        openModal(document.getElementById("modalNovoPedido"));
    }
	
}

// Exibição automática de mensagens temporárias
function initAutoHideMessages() {

    const messages = document.querySelectorAll(".message");

    messages.forEach((message) => {

        if (!message.textContent.trim()) {
            return;
        }

        setTimeout(() => {

            message.style.transition = "opacity 0.5s ease";

            message.style.opacity = "0";

            setTimeout(() => {
                message.remove();
            }, 500);

        }, 3000);
    });
}

// Armazenamento temporário dos dados dos pedidos
const ORDER_STATE_KEY = "pediuPartiuOrderState";

function initSaveOrderStateBeforeClientRedirect() {
    document.addEventListener("click", (event) => {
        const button = event.target.closest("[data-save-order-state]");

        if (!button) return;

        const form = button.closest("[data-order-form]");

        if (!form) return;

        saveOrderFormState(form);
    });
}

function saveOrderFormState(form) {
    const modal = form.closest(".modal");

    const state = {
        modalId: modal ? modal.id : "",
        fields: {},
        itens: []
    };

    form.querySelectorAll("input, select, textarea").forEach((field) => {
        if (!field.name || field.name === "produtos" || field.name === "quantidades") {
            return;
        }

        state.fields[field.name] = field.value;
    });

    form.querySelectorAll("[data-order-row]").forEach((row) => {
        const produto = row.querySelector("[data-order-product]");
        const quantidade = row.querySelector("[data-order-qty]");

        state.itens.push({
            produto: produto ? produto.value : "",
            quantidade: quantidade ? quantidade.value : "1"
        });
    });

    sessionStorage.setItem(ORDER_STATE_KEY, JSON.stringify(state));
}

function initRestoreOrderStateFromUrl() {
    const params = new URLSearchParams(window.location.search);

    let modal = null;

    if (params.get("abrirNovoPedido") === "true") {
        modal = document.getElementById("modalNovoPedido");
    }

    if (params.get("abrirEditarPedido")) {
        modal = document.getElementById("modalEditarPedido" + params.get("abrirEditarPedido"));
    }

    if (!modal) return;

    openModal(modal);
    restoreOrderFormState(modal);
}

function restoreOrderFormState(modal) {
    const rawState = sessionStorage.getItem(ORDER_STATE_KEY);

    if (!rawState) return;

    const state = JSON.parse(rawState);

    if (state.modalId && state.modalId !== modal.id) {
        return;
    }

    const form = modal.querySelector("[data-order-form]");

    if (!form) return;

    Object.entries(state.fields).forEach(([name, value]) => {
        const field = form.querySelector(`[name="${name}"]`);

        if (field) {
            field.value = value;
        }
    });

    let rows = form.querySelectorAll("[data-order-row]");

    while (rows.length < state.itens.length) {
        const addButton = form.querySelector("[data-add-order-item]");

        if (!addButton) break;

        addOrderItemRow(addButton);
        rows = form.querySelectorAll("[data-order-row]");
    }

    rows.forEach((row, index) => {
        const item = state.itens[index];

        if (!item) {
            row.remove();
            return;
        }

        const produto = row.querySelector("[data-order-product]");
        const quantidade = row.querySelector("[data-order-qty]");

        if (produto) produto.value = item.produto;
        if (quantidade) quantidade.value = item.quantidade;
    });

    updateOrderFormTotal(form);
    prepareForms(modal);

    sessionStorage.removeItem(ORDER_STATE_KEY);
}

// Controle de retorno para a tela de pedidos
function deveVoltarParaPedido() {
    const params = new URLSearchParams(window.location.search);
    return params.get("voltarPedido") === "true";
}

function montarUrlRetornoPedido() {
    const params = new URLSearchParams(window.location.search);

    if (params.get("origemPedido") === "editar" && params.get("idPedido")) {
        return `/pedidos?abrirEditarPedido=${params.get("idPedido")}`;
    }

    return "/pedidos?abrirNovoPedido=true";
}