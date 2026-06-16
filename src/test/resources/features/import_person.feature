Feature: Person CSV Import API

  Scenario: Successfully import a valid CSV file
    When I POST to the import endpoint with filePath "classpath:test-people.csv"
    Then the response status should be 200
    And the response body should be "CSV import completed successfully"

  Scenario: Import fails when filePath parameter is missing
    When I POST to the import endpoint without a filePath parameter
    Then the response status should be 400

  Scenario: Import fails when the file does not exist
    When I POST to the import endpoint with filePath "/nonexistent/path/people.csv"
    Then the response status should be 500
    And the response body should contain "CSV import failed"