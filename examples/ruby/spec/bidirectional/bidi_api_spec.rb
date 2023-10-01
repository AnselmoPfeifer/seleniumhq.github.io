# frozen_string_literal: true

require 'spec_helper'

RSpec.describe 'BiDi API' do
  let(:driver) { start_session }

  it 'does basic authentication' do
    driver.register(username: 'admin', password: 'admin')

    driver.get('https://the-internet.herokuapp.com/basic_auth')

    expect(driver.find_element(tag_name: 'p').text).to eq('Congratulations! You must have the proper credentials.')
  end

  it 'pins script' do
    is_displayed = Class.new.extend(Selenium::WebDriver::Atoms).atom_script(:isDisplayed)
    script = driver.pin_script(is_displayed)
    expect(driver.pinned_scripts).to eq([script])

    driver.get('https://www.selenium.dev/selenium/web/javascriptPage.html')
    visible = driver.find_element(id: 'visibleSubElement')
    hidden = driver.find_element(id: 'hiddenlink')

    expect(driver.execute_script(script, visible)).to eq true
    expect(driver.execute_script(script, hidden)).to eq false
  end
end
