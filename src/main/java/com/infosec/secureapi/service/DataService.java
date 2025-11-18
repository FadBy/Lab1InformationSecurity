package com.infosec.secureapi.service;

import com.infosec.secureapi.dto.DataItemRequest;
import com.infosec.secureapi.dto.DataItemResponse;
import com.infosec.secureapi.entity.DataItem;
import com.infosec.secureapi.entity.User;
import com.infosec.secureapi.repository.DataItemRepository;
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
        dataItem.setTitle(sanitizeInput(request.getTitle()));
        dataItem.setContent(sanitizeInput(request.getContent()));
        dataItem.setUser(user);
        
        DataItem saved = dataItemRepository.save(dataItem);
        return convertToResponse(saved);
    }

    private DataItemResponse convertToResponse(DataItem item) {
        DataItemResponse response = new DataItemResponse();
        response.setId(item.getId());
        response.setTitle(item.getTitle());
        response.setContent(item.getContent());
        response.setUsername(item.getUser().getUsername());
        response.setCreatedAt(item.getCreatedAt());
        return response;
    }

    /**
     * Санитизация пользовательского ввода для защиты от XSS
     * Экранирует HTML символы
     */
    private String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;")
                .replace("/", "&#x2F;");
    }
}

