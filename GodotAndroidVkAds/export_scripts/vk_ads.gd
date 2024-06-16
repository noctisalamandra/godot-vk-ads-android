extends Node

class_name VkAds

# Signals
# Banner
signal banner_loaded
signal banner_failed_to_load(error_code)

# Interstitial
signal interstitial_loaded
signal interstitial_failed_to_load(error_code)
signal interstitial_closed

# Rewarded
signal rewarded(type)
signal rewarded_video_loaded
signal rewarded_video_failed_to_load(error_code)
signal rewarded_video_closed

# Properties
@export var banner_id:int
@export var banner_on_top:bool = false
@export var interstitial_id:int
@export var rewarded_id:int

# "Private" properties
var _vk_singleton = null
var _is_interstitial_loaded:bool = false
var _is_rewarded_video_loaded:bool = false

func _enter_tree():
	if not init():
		print("Vk Java Singleton not found. This plugin will only work on Android")

# Initialization
func init() -> bool:
	if(Engine.has_singleton("GodotAndroidVkAds")):
		_vk_singleton = Engine.get_singleton("GodotAndroidVkAds")
		if not _vk_singleton.is_connected("_on_banner_loaded", _on_banner_loaded):
			connect_signals()
			_vk_singleton.init()
			return true
	return false

# Connect the VkAds Java signals
func connect_signals() -> void:
	# Banner
	_vk_singleton._on_banner_loaded.connect(_on_banner_loaded)
	_vk_singleton._on_banner_failed_to_load.connect(_on_banner_failed_to_load)

	# Interstitial
	_vk_singleton._on_interstitial_loaded.connect(_on_interstitial_loaded)
	_vk_singleton._on_interstitial_failed_to_load.connect(_on_interstitial_failed_to_load)
	_vk_singleton._on_interstitial_ad_dismissed.connect(_on_interstitial_ad_dismissed)

	# Rewarded
	_vk_singleton._on_rewarded.connect(_on_rewarded)
	_vk_singleton._on_rewarded_video_ad_loaded.connect(_on_rewarded_video_ad_loaded)
	_vk_singleton._on_rewarded_video_ad_failed_to_load.connect(_on_rewarded_video_ad_failed_to_load)
	_vk_singleton._on_rewarded_video_ad_dismissed.connect(_on_rewarded_video_ad_dismissed)

# Load
func load_banner() -> void:
	if _vk_singleton != null:
		_vk_singleton.loadBanner(banner_id, banner_on_top)

func load_interstitial() -> void:
	if _vk_singleton != null:
		_vk_singleton.loadInterstitial(interstitial_id)

func is_interstitial_loaded() -> bool:
	if _vk_singleton != null:
		return _is_interstitial_loaded
	return false

func load_rewarded_video() -> void:
	if _vk_singleton != null:
		_vk_singleton.loadRewardedVideo(rewarded_id)

func is_rewarded_video_loaded() -> bool:
	if _vk_singleton != null:
		return _is_rewarded_video_loaded
	return false

# Show / hide
func show_banner() -> void:
	if _vk_singleton != null:
		_vk_singleton.showBanner()

func hide_banner() -> void:
	if _vk_singleton != null:
		_vk_singleton.hideBanner()

func show_interstitial() -> void:
	if _vk_singleton != null:
		_vk_singleton.showInterstitial()
		_is_interstitial_loaded = false

func show_rewarded_video() -> void:
	if _vk_singleton != null:
		_vk_singleton.showRewardedVideo()
		_is_rewarded_video_loaded = false

# Dimension
func get_banner_dimension() -> Vector2:
	if _vk_singleton != null:
		return Vector2(_vk_singleton.getBannerWidth(), _vk_singleton.getBannerHeight())
	return Vector2()

# Callbacks
# Banner
func _on_banner_loaded() -> void:
	emit_signal("banner_loaded")

func _on_banner_failed_to_load(error_code:int) -> void:
	emit_signal("banner_failed_to_load", error_code)

# Interstitial
func _on_interstitial_loaded() -> void:
	_is_interstitial_loaded = true
	emit_signal("interstitial_loaded")

func _on_interstitial_failed_to_load(error_code:int) -> void:
	_is_interstitial_loaded = false
	emit_signal("interstitial_failed_to_load", error_code)

func _on_interstitial_ad_dismissed() -> void:
	emit_signal("interstitial_closed")

# Rewarded
func _on_rewarded(type:String) -> void:
	emit_signal("rewarded", type)

func _on_rewarded_video_ad_loaded() -> void:
	_is_rewarded_video_loaded = true
	emit_signal("rewarded_video_loaded")

func _on_rewarded_video_ad_failed_to_load(error_code:int) -> void:
	_is_rewarded_video_loaded = false
	emit_signal("rewarded_video_failed_to_load", error_code)

func _on_rewarded_video_ad_dismissed() -> void:
	emit_signal("rewarded_video_closed")
