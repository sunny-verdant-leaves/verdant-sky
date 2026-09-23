"""
生物群系出生概率估算器
使用方法 python <脚本路径> <world_preset路径> <noise_settings路径>

Biome Spawn Probability Estimator
Usage: python <script path> <world_preset path> <noise_settings path>
"""

import json
import random
import sys
from collections import defaultdict

def load_json(path):
    with open(path, 'r', encoding='utf-8') as f:
        return json.load(f)

def get_overworld_biomes(world_preset):
    dims = world_preset.get('dimensions', {})
    overworld = dims.get('minecraft:overworld', {})
    generator = overworld.get('generator', {})
    biome_source = generator.get('biome_source', {})
    return biome_source.get('biomes', [])

def get_spawn_target(noise_settings):
    return noise_settings.get('spawn_target', [])

def merge_spawn_target(spawn_targets):
    params = ['temperature', 'humidity', 'continentalness', 'erosion', 'weirdness', 'depth']
    ranges = {}
    for p in params:
        mins = []
        maxs = []
        for st in spawn_targets:
            val = st.get(p)
            if val is None:
                continue
            if isinstance(val, list):
                mins.append(val[0])
                maxs.append(val[1])
            else:
                mins.append(val)
                maxs.append(val)
        if mins:
            ranges[p] = (min(mins), max(maxs))
        else:
            ranges[p] = (-1.0, 1.0)
    return ranges

def distance_to_range(value, rng):
    """计算 value 到范围 [min, max] 的距离，若在范围内则为 0"""
    if isinstance(rng, list):
        mn, mx = rng[0], rng[1]
    else:
        mn = mx = rng
    if value < mn:
        return mn - value
    elif value > mx:
        return value - mx
    else:
        return 0.0

def biome_distance(point, biome_params):
    """计算采样点到生物群系参数的总距离（欧几里得距离的平方和，简化版）"""
    total = 0.0
    for param, val in point.items():
        if param not in biome_params:
            continue
        rng = biome_params[param]
        d = distance_to_range(val, rng)
        total += d * d
    return total

def estimate(world_preset_path, noise_settings_path, samples=200000):
    world_preset = load_json(world_preset_path)
    noise_settings = load_json(noise_settings_path)
    
    biomes = get_overworld_biomes(world_preset)
    spawn_targets = get_spawn_target(noise_settings)
    
    if not biomes:
        print("错误：未找到主世界生物群系列表")
        return
    if not spawn_targets:
        print("错误：未找到 spawn_target")
        return
    
    ranges = merge_spawn_target(spawn_targets)
    print("采样参数范围（并集）：")
    for p, (mn, mx) in ranges.items():
        print(f"  {p}: [{mn:.3f}, {mx:.3f}]")
    
    counts = defaultdict(int)
    total = 0
    for _ in range(samples):
        point = {}
        for param, (minv, maxv) in ranges.items():
            if minv == maxv:
                point[param] = minv
            else:
                point[param] = random.uniform(minv, maxv)
        
        # 找到距离最小的生物群系
        best_biome = None
        best_dist = float('inf')
        for biome_entry in biomes:
            biome_name = biome_entry['biome']
            params = biome_entry['parameters']
            dist = biome_distance(point, params)
            if dist < best_dist:
                best_dist = dist
                best_biome = biome_name
        
        if best_biome:
            counts[best_biome] += 1
        total += 1
    
    print(f"\n总采样数: {total}")
    print("生物群系出生概率估算：")
    print("-" * 50)
    for biome, cnt in sorted(counts.items(), key=lambda x: -x[1]):
        prob = cnt / total * 100
        print(f"{biome:40s} {prob:6.2f}%")
    print("-" * 50)
    print(f"未匹配到任何生物群系的采样点: {total - sum(counts.values())} 次")

if __name__ == '__main__':
    if len(sys.argv) < 3:
        print("用法: python spawn_estimator.py <world_preset.json> <noise_settings.json> [采样数]")
        sys.exit(1)
    world_preset_path = sys.argv[1]
    noise_settings_path = sys.argv[2]
    samples = int(sys.argv[3]) if len(sys.argv) > 3 else 200000
    estimate(world_preset_path, noise_settings_path, samples)