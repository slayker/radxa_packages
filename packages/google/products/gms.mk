#
# Copyright (C) 2008 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# GMS Mandatory Apps and libraries
PRODUCT_PACKAGES += \
    GoogleBackupTransport \
    ConfigUpdater \
    GoogleFeedback \
    GoogleServicesFramework \
    GoogleLoginService \
    GooglePartnerSetup \
    SetupWizard \
    GoogleContactsSyncAdapter \
    GoogleCalendarSyncAdapter \
    Phonesky \
    NetworkLocation \
    com.google.android.media.effects.jar \
    google_generic_update.txt

# Chrome Browser: Chrome is the only preloaded system browser
# Use "ChromeWithBrowser" if Chrome is preloaded along with another browser side-by-sde
PRODUCT_PACKAGES += Chrome
#PRODUCT_PACKAGES += ChromeWithBrowser
#PRODUCT_PACKAGES += ChromeBookmarksSyncAdapter

# GMS Mandatory Apps (unbundled)
PRODUCT_PACKAGES += \
    Books \
    Drive \
    Magazines \
    Maps \
    PlusOne \
    Gmail2 \
    GmsCore \
    Velvet \
    Music2 \
    PlayGames \
    Street \
    Hangouts \
    Videos \
    YouTube

# GMS Optional Apps
PRODUCT_PACKAGES += \
    VoiceSearchStub \
    DeskClockGoogle \
    FaceLock \
    GalleryGoogle \
    MediaUploader \
    VideoEditorGoogle \
    TagGoogle \
    CalendarGoogle \
    OneTimeInitializer \
    LatinImeGoogle \
    talkback

# Include GoogleTTS and TTS languages needed for GoogleTTS
PRODUCT_PACKAGES += GoogleTTS
include vendor/google/products/text_to_speech_languages.mk

# Overlay for Google network and fused location providers
$(call inherit-product, device/sample/products/location_overlay.mk)

# Overrides
PRODUCT_PROPERTY_OVERRIDES += \
     ro.setupwizard.mode=OPTIONAL \
     ro.com.google.gmsversion=4.2_r5
