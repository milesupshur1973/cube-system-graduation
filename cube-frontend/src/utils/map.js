import AMapLoader from '@amap/amap-jsapi-loader';

// 填入高德凭证
const AMAP_KEY = '82dcca6c44461c446d9ba77e908d3066';
const SECURITY_CODE = 'ba9762886bb437471a2ed97a2005e800';

let AMapInstance = null;

/**
 * 1. 初始化地图基础配置
 */
export const initAMap = async () => {
    if (AMapInstance) return AMapInstance;

    window._AMapSecurityConfig = {
        securityJsCode: SECURITY_CODE,
    };

    try {
        AMapInstance = await AMapLoader.load({
            key: AMAP_KEY,
            version: "2.0",
            plugins: ['AMap.Geolocation', 'AMap.Geocoder']
        });
        return AMapInstance;
    } catch (e) {
        console.error("高德地图加载失败", e);
        throw e;
    }
};

/**
 * 2. 获取用户当前精准经纬度
 */
export const getUserLocation = () => {
    return new Promise((resolve, reject) => {
        if (!AMapInstance) return reject("地图未初始化");

        const geolocation = new AMapInstance.Geolocation({
            enableHighAccuracy: true, // 开启高精度
            timeout: 5000,            // 5秒超时
        });

        geolocation.getCurrentPosition((status, result) => {
            if (status === 'complete') {
                resolve(result.position); // 返回 {lng, lat}
            } else {
                reject(result.message);   // 定位失败（如用户拒绝授权）
            }
        });
    });
};

/**
 * 3. 将一段文本地址转为经纬度
 */
const geocodeAddress = (address) => {
    return new Promise((resolve) => {
        const geocoder = new AMapInstance.Geocoder();
        geocoder.getLocation(address, (status, result) => {
            if (status === 'complete' && result.geocodes.length) {
                resolve(result.geocodes[0].location);
            } else {
                resolve(null); // 解析失败
            }
        });
    });
};

/**
 * 4. 核心功能：计算距离并重组排序
 */
export const calculateDistancesAndSort = async (userPos, compList) => {
    const processedList = [];

    // 循环解析每个赛事的地址
    for (let comp of compList) {
        const address = `${comp.province}${comp.city}${comp.location}`;
        const targetPos = await geocodeAddress(address);

        if (targetPos) {
            // 计算两点之间的直线距离（单位：米）
            const distance = AMapInstance.GeometryUtil.distance(
                [userPos.lng, userPos.lat],
                [targetPos.lng, targetPos.lat]
            );
            comp.distanceNum = distance; // 用于排序
            comp.distanceText = (distance / 1000).toFixed(1) + ' km'; // 用于页面展示，比如 3.2 km
        } else {
            comp.distanceNum = 99999999; // 解析失败的沉底
            comp.distanceText = '距离未知';
        }
        processedList.push(comp);
    }

    // 按照距离从小到大排序
    processedList.sort((a, b) => a.distanceNum - b.distanceNum);
    return processedList;
};