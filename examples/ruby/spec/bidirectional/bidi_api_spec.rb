# frozen_string_literal: true

require 'spec_helper'

RSpec.describe 'BiDi API' do
  let(:driver) { start_session }

  it 'does basic authentication' do
    driver.register(username: 'admin',
                    password: 'admin',
                    uri: /herokuapp/)

    driver.get('https://the-internet.herokuapp.com/basic_auth')

    expect(driver.find_element(tag_name: 'p').text).to eq('Congratulations! You must have the proper credentials.')
  end

  it 'pins script' do
    driver.get('https://www.selenium.dev/selenium/web/javascriptPage.html')
    is_displayed_script = Class.new.extend(Selenium::WebDriver::Atoms).atom_script(:isDisplayed)

    script = driver.pin_script(is_displayed_script)

    visible = driver.find_element(id: 'visibleSubElement')
    hidden = driver.find_element(id: 'hiddenlink')

    visible_displayed = driver.execute_script(script, visible)
    hidden_displayed = driver.execute_script(script, hidden)

    expect(visible_displayed).to eq true
    expect(hidden_displayed).to eq false
  end
end
