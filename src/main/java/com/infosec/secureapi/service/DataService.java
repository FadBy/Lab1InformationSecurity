package com.infosec.secureapi.service;

import com.infosec.secureapi.dto.DataItemRequest;
import com.infosec.secureapi.dto.DataItemResponse;
import com.infosec.secureapi.entity.DataItem;
import com.infosec.secureapi.entity.User;
import com.infosec.secureapi.repository.DataItemRepository;
import org.owasp.encoder.Encode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DataService {
    private final DataItemRepository dataItemRepository;

    @Autowired
    public DataService(DataItemRepository dataItemRepository) {
        this.dataItemRepository = dataItemRepository;
    }

    @Transactional(readOnly = true)
    public List<DataItemResponse> getAllDataItems() {
        return dataItemRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public DataItemResponse createDataItem(DataItemRequest request, User user) {
        DataItem dataItem = new DataItem();
        // Сохраняем данные как есть (без экранирования) в БД
        dataItem.setTitle(request.getTitle());
        dataItem.setContent(request.getContent());
        dataItem.setUser(user);
        
        DataItem saved = dataItemRepository.save(dataItem);
        // Санитизируем при возврате (output encoding)
        return convertToResponse(saved);
    }

    /**
     * Конвертация сущности в DTO с санитизацией всех строковых полей
     * Санитизация применяется при выходе (output encoding) для защиты от XSS
     */
    private DataItemResponse convertToResponse(DataItem item) {
        DataItemResponse response = new DataItemResponse();
        response.setId(item.getId());
        // Санитизируем все пользовательские данные при возврате
        response.setTitle(escapeHtml(item.getTitle()));
        response.setContent(escapeHtml(item.getContent()));
        response.setUsername(escapeHtml(item.getUser().getUsername()));
        response.setCreatedAt(item.getCreatedAt());
        return response;
    }

    /**
     * Экранирование HTML символов для защиты от XSS (Cross-Site Scripting)
     * Применяется ко всем данным, возвращаемым в API ответах
     * 
     * Использует OWASP Java Encoder - проверенную библиотеку для защиты от XSS,
     * которая покрывает все edge cases и соответствует рекомендациям OWASP.
     * 
     * Экранирует опасные HTML символы:
     * - & -> &amp;
     * - < -> &lt;
     * - > -> &gt;
     * - " -> &quot;
     * - ' -> &#x27;
     * - / -> &#x2F;
     * - и другие потенциально опасные символы
     * 
     * @param input исходная строка
     * @return экранированная строка, безопасная для вывода в HTML/JSON
     */
    public static String escapeHtml(String input) {
        if (input == null) {
            return null;
        }
        // Используем OWASP Java Encoder для надежной защиты от XSS
        return Encode.forHtml(input);
    }
}

