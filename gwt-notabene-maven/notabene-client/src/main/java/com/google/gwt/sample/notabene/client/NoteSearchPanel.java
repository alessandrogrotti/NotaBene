package com.google.gwt.sample.notabene.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.sample.notabene.shared.NoteFilter;
import com.google.gwt.sample.notabene.shared.Tag;
import com.google.gwt.sample.notabene.shared.TagService;
import com.google.gwt.sample.notabene.shared.TagServiceAsync;
import java.util.List;
import java.util.HashSet;

public class NoteSearchPanel extends Composite {
    
    private final TagServiceAsync tagService = GWT.create(TagService.class);
    
    private VerticalPanel mainPanel;
    
    private TextBox keywordBox;
    private ListBox tagListBox;
    private Button clearButton;
    private HorizontalPanel selectedTagsPanel;
    
    private TextBox authorBox;
    private DateBox startDateBox;
    private DateBox endDateBox;
    private Button toggleFiltersButton;
    
    private VerticalPanel advancedFiltersPanel;
    private boolean filtersVisible = false;
    
    private NoteFilter currentFilter;
    
    private SearchHandler searchHandler;
    
    private Timer searchTimer;
    
    public interface SearchHandler {
        void onSearch(NoteFilter filter);
    }
    
    public NoteSearchPanel() {
        currentFilter = new NoteFilter();
        initializeComponents();
        setupEventHandlers();
        loadAvailableTags();
    }
    
    
    private void initializeComponents() {
        mainPanel = new VerticalPanel();
        mainPanel.setWidth("100%");
        mainPanel.setStyleName("simple-search-panel");
        mainPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
        
        VerticalPanel contentPanel = new VerticalPanel();
        contentPanel.setStyleName("search-content-container");
        
        HorizontalPanel firstRow = new HorizontalPanel();
        firstRow.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
        firstRow.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
        firstRow.setStyleName("search-row");
        
        HorizontalPanel searchGroup = new HorizontalPanel();
        searchGroup.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
        
        Label searchLabel = new Label("Cerca:");
        searchLabel.setStyleName("search-field-label");
        
        keywordBox = new TextBox();
        keywordBox.setStyleName("form-input");
        keywordBox.setWidth("300px");
        keywordBox.getElement().setPropertyString("placeholder", "Cerca nelle tue note...");
        
        searchGroup.add(searchLabel);
        searchGroup.add(keywordBox);
        
        HorizontalPanel tagGroup = new HorizontalPanel();
        tagGroup.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
        
        Label tagLabel = new Label("Tag:");
        tagLabel.setStyleName("search-field-label");
        
        tagListBox = new ListBox();
        tagListBox.setStyleName("form-input");
        tagListBox.setWidth("150px");
        
        tagGroup.add(tagLabel);
        tagGroup.add(tagListBox);
        
        firstRow.add(searchGroup);
        firstRow.add(tagGroup);
        
        HorizontalPanel secondRow = new HorizontalPanel();
        secondRow.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
        secondRow.setStyleName("search-buttons-row");
        
        toggleFiltersButton = new Button("Filtri Avanzati");
        toggleFiltersButton.setStyleName("toggle-advanced-filters-btn");
        
        clearButton = new Button("Pulisci");
        clearButton.setStyleName("clear-search-btn");
        
        secondRow.add(toggleFiltersButton);
        secondRow.add(clearButton);
        
        contentPanel.add(firstRow);
        selectedTagsPanel = new HorizontalPanel();
        selectedTagsPanel.setStyleName("inline-tags");
        contentPanel.add(selectedTagsPanel);
        contentPanel.add(secondRow);
        
        mainPanel.add(contentPanel);
        
        advancedFiltersPanel = createAdvancedFiltersPanel();
        advancedFiltersPanel.setVisible(false);
        mainPanel.add(advancedFiltersPanel);
        
        initWidget(mainPanel);
    }
    
    private VerticalPanel createAdvancedFiltersPanel() {
        VerticalPanel filtersPanel = new VerticalPanel();
        filtersPanel.setStyleName("advanced-filters-panel");
        filtersPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
        
        Label filtersTitle = new Label("Filtri Avanzati");
        filtersTitle.setStyleName("advanced-filters-title");
        filtersPanel.add(filtersTitle);
        
        FlexTable filtersTable = new FlexTable();
        filtersTable.setStyleName("filters-container");
        
        int row = 0;
        
        Label authorLabel = new Label("Autore:");
        authorLabel.setStyleName("filter-field-label");
        filtersTable.setWidget(row, 0, authorLabel);
        
        authorBox = new TextBox();
        authorBox.setStyleName("form-input");
        authorBox.setWidth("200px");
        authorBox.getElement().setPropertyString("placeholder", "Nome autore...");
        filtersTable.setWidget(row, 1, authorBox);
        
        Label fromLabel = new Label("Dal:");
        fromLabel.setStyleName("date-sublabel");
        filtersTable.setWidget(row, 2, fromLabel);
        
        startDateBox = new DateBox();
        startDateBox.setStyleName("form-input");
        startDateBox.setWidth("130px");
        filtersTable.setWidget(row, 3, startDateBox);
        
        Label toLabel = new Label("Al:");
        toLabel.setStyleName("date-sublabel");
        filtersTable.setWidget(row, 4, toLabel);
        
        endDateBox = new DateBox();
        endDateBox.setStyleName("form-input");
        endDateBox.setWidth("130px");
        filtersTable.setWidget(row, 5, endDateBox);
        
        for (int col = 0; col < 6; col++) {
            filtersTable.getCellFormatter().setVerticalAlignment(row, col, HasVerticalAlignment.ALIGN_MIDDLE);
            filtersTable.getCellFormatter().setHorizontalAlignment(row, col, HasHorizontalAlignment.ALIGN_CENTER);
        }
        
        filtersPanel.add(filtersTable);
        
        return filtersPanel;
    }
    
    private void setupEventHandlers() {
        keywordBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                if (searchTimer != null) {
                    searchTimer.cancel();
                }
                searchTimer = new Timer() {
                    @Override
                    public void run() {
                        performSearch();
                    }
                };
                searchTimer.schedule(200);
            }
        });
        
        authorBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                if (searchTimer != null) {
                    searchTimer.cancel();
                }
                searchTimer = new Timer() {
                    @Override
                    public void run() {
                        performSearch();
                    }
                };
                searchTimer.schedule(300);
            }
        });
        
        startDateBox.addValueChangeHandler(event -> performSearch());
        endDateBox.addValueChangeHandler(event -> performSearch());
        
        tagListBox.addChangeHandler(event -> {
            int selectedIndex = tagListBox.getSelectedIndex();
            if (selectedIndex > 0) { // > 0 perché 0 è "-- Tutti i tag --"
                String selectedTag = tagListBox.getValue(selectedIndex);
                if (!selectedTag.isEmpty() && !currentFilter.getTags().contains(selectedTag)) {
                    currentFilter.addTag(selectedTag);
                    addTagToSelectedPanel(selectedTag);
                    tagListBox.setSelectedIndex(0); // Reset alla selezione default
                    performSearch();
                }
            }
        });
        
        toggleFiltersButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                toggleAdvancedFilters();
            }
        });
        
        clearButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                clearFilters();
            }
        });
    }
    
    private void toggleAdvancedFilters() {
        filtersVisible = !filtersVisible;
        advancedFiltersPanel.setVisible(filtersVisible);
        toggleFiltersButton.setText(filtersVisible ? "⚙️ Nascondi Filtri" : "Filtri Avanzati");
    }
    
    private void performSearch() {
        updateCurrentFilter();
        if (searchHandler != null) {
            searchHandler.onSearch(currentFilter);
        }
    }
    
    private void updateCurrentFilter() {
        currentFilter.setKeyword(keywordBox.getText().trim());
        currentFilter.setSearchInTitle(true);
        currentFilter.setSearchInContent(true);
        
        currentFilter.setAuthor(authorBox.getText().trim());
        
        currentFilter.setStartDate(startDateBox.getValue());
        currentFilter.setEndDate(endDateBox.getValue());
    }
    
    private void clearFilters() {
        keywordBox.setText("");
        authorBox.setText("");
        startDateBox.setValue(null);
        endDateBox.setValue(null);
        currentFilter.clear();
        selectedTagsPanel.clear();
        performSearch();
    }
    
    public void clearAllFilters() {
        clearFilters();
    }
    
    private void addTagToSelectedPanel(String tag) {
        HorizontalPanel tagItem = new HorizontalPanel();
        tagItem.setStyleName("simple-selected-tag");
        
        Label tagLabel = new Label(tag);
        tagLabel.setStyleName("simple-tag-text");
        
        Button removeButton = new Button("✕");
        removeButton.setStyleName("simple-remove-tag");
        removeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                currentFilter.removeTag(tag);
                selectedTagsPanel.remove(tagItem);
                performSearch();
            }
        });
        
        tagItem.add(tagLabel);
        tagItem.add(removeButton);
        selectedTagsPanel.add(tagItem);
    }
    
    private void loadAvailableTags() {
        tagService.getAllTags(new AsyncCallback<List<Tag>>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Errore nel caricamento dei tag: " + caught.getMessage());
                tagListBox.addItem("-- Tutti i tag --", "");
            }
            
            @Override
            public void onSuccess(List<Tag> tags) {
                tagListBox.clear();
                tagListBox.addItem("-- Tutti i tag --", "");
                if (tags != null && !tags.isEmpty()) {
                    for (Tag tag : tags) {
                        tagListBox.addItem(tag.getName(), tag.getName());
                    }
                }
            }
        });
    }
    
    public void setSearchHandler(SearchHandler handler) {
        this.searchHandler = handler;
    }
    
    public NoteFilter getCurrentFilter() {
        updateCurrentFilter();
        return currentFilter;
    }
    
    public void setKeyword(String keyword) {
        keywordBox.setText(keyword != null ? keyword : "");
    }
    
    public void focusKeywordBox() {
        keywordBox.setFocus(true);
    }
}
